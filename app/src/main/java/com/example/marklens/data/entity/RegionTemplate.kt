package com.example.marklens.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "region_templates")
data class RegionTemplate(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val regionsJson: String
)
