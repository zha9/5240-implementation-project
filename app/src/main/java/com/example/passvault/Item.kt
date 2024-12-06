package com.example.passvault

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity
data class Item(
    @PrimaryKey val id: UUID,
    var title: String,
    var userName: String,
    var passWord: String,
    var note: String
)