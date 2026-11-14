package com.siit.recipeapp.data.local;

import androidx.room.TypeConverter;
import java.time.LocalDate;
import java.time.LocalDateTime;
public final class Converters {

    private Converters(){}

    @TypeConverter
    public static String fromLocalDate(LocalDate value){
        return value == null ? null : value.toString();
    }

    @TypeConverter
    public static LocalDate toLocalDate(String value){
        return value == null ? null : LocalDate.parse(value);
    }

    @TypeConverter
    public static String fromLocalDateTime(LocalDateTime value){
        return value == null ? null : value.toString();
    }

    @TypeConverter
    public static LocalDateTime toLocalDateTime(String value){
        return value == null ? null : LocalDateTime.parse(value);
    }
}
