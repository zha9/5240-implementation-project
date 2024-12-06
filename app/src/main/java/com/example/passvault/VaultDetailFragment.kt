package com.example.passvault

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.passvault.databinding.FragmentDetailVaultBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import java.security.MessageDigest
import okhttp3.OkHttpClient
import okhttp3.Request

class VaultDetailFragment : Fragment() {
    private var _binding: FragmentDetailVaultBinding? = null
    private var isPasswordVisible = false
    private val binding
        get() = checkNotNull(_binding) {
            "Null binding!"
        }

    private val args: VaultDetailFragmentArgs by navArgs()

    private val vaultDetailViewModel: VaultDetailViewModel by viewModels {
        VaultDetailViewModelFactory(args.itemId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            FragmentDetailVaultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            itemTitle.doOnTextChanged { text, _, _, _ ->
                vaultDetailViewModel.updateItem { oldItem ->
                    oldItem.copy(title = text.toString())
                }
            }

            itemUsername.doOnTextChanged { text, _, _, _ ->
                vaultDetailViewModel.updateItem { oldItem ->
                    oldItem.copy(userName = text.toString())
                }
            }

            itemPassword.doOnTextChanged { text, _, _, _ ->
                vaultDetailViewModel.updateItem { oldItem ->
                    oldItem.copy(passWord = text.toString())
                }
            }

            itemNote.doOnTextChanged { text, _, _, _ ->
                vaultDetailViewModel.updateItem { oldItem ->
                    oldItem.copy(note = text.toString())
                }
            }

            visibilityToggle.setOnClickListener {
                isPasswordVisible = !isPasswordVisible
                if (isPasswordVisible) {
                    itemPassword.inputType =
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    visibilityToggle.setImageResource(R.drawable.baseline_visibility_24)
                }
                else {
                    itemPassword.inputType =
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    visibilityToggle.setImageResource(R.drawable.baseline_visibility_off_24)
                }
                itemPassword.setSelection(itemPassword.text?.length ?: 0)
            }

            copyPassword.setOnClickListener {
                val passwordText = itemPassword.text.toString()
                if (passwordText.isNotEmpty()) {
                    val clipboard =
                        requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Password", passwordText)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(requireContext(), "Password copied to clipboard!", Toast.LENGTH_SHORT).show()
                }
            }

            binding.shareButton.setOnClickListener {
                val noteText = binding.itemNote.text.toString()
                if (noteText.isNotEmpty()) {
                    val shareIntent = Intent(Intent.ACTION_SEND)
                    shareIntent.setType("text/plain")
                    shareIntent.putExtra(Intent.EXTRA_TEXT, noteText)
                    val chooser = Intent.createChooser(shareIntent, "Send this note?")
                    startActivity(chooser)
                }
            }

            binding.checkPassword.setOnClickListener {
                val password = binding.itemPassword.text.toString()
                if (password.isBlank()) {
                    Toast.makeText(requireContext(), "Password is empty!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        val count = passCheckAPI(password)
                        if (count > 0) {
                            Toast.makeText(
                                requireContext(),
                                "This password has been leaked $count times!",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "This password has not been leaked!",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vaultDetailViewModel.item.collect { item ->
                    item?.let { updateUi(it) }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_vault_detail, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.delete_item -> {
                deleteCurrentItem()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun deleteCurrentItem() {
        vaultDetailViewModel.deleteItem()
        findNavController().navigateUp()
    }

    private fun sha1(input: String): String {
        val digest = MessageDigest.getInstance("SHA-1")
        val bytes = digest.digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    suspend fun passCheckAPI(password: String): Int {
        val client = OkHttpClient()
        val hash = sha1(password).uppercase()
        val prefix = hash.substring(0, 5)
        val suffix = hash.substring(5)

        val url = "https://api.pwnedpasswords.com/range/$prefix"
        val request = Request.Builder()
            .url(url)
            .build()

        return withContext(Dispatchers.IO) {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw Exception("Failed to fetch data: HTTP ${response.code}")
            }

            val result = response.body?.string()
                ?: throw Exception("Empty response from API")

            val lines = result.split("\n")
            for (line in lines) {
                val parts = line.split(":")
                if (parts[0].trim() == suffix) {
                    return@withContext parts[1].trim().toInt()
                }
            }
            return@withContext 0
        }
    }

    private fun updateUi(item: Item) {
        binding.apply {
            if (itemTitle.text.toString() != item.title) {
                itemTitle.setText(item.title)
            }
            if (itemUsername.text.toString() != item.userName) {
                itemUsername.setText(item.userName)
            }
            if (itemPassword.text.toString() != item.passWord) {
                itemPassword.setText(item.passWord)
            }
            if (itemNote.text.toString() != item.note) {
                itemNote.setText(item.note)
            }
        }
    }
}