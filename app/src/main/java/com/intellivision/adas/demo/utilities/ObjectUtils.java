package com.intellivision.adas.demo.utilities;

import java.lang.reflect.Field;

import com.intellivision.adas.demo.logger.Category;
import com.intellivision.adas.demo.logger.VCLog;

public class ObjectUtils {

    public static void displayObject( Object obj ) {
        try {
            VCLog.error( Category.CAT_GENERAL, "Testing:Class : " + obj.getClass( ).getName( ) );
            for ( Field field : obj.getClass( ).getDeclaredFields( ) ) {
                field.setAccessible( true );
                String name = field.getName( );
                Object value = field.get( obj );
                String log = String.format( "Testing:%s : %s%n", name, value );
                VCLog.error( Category.CAT_GENERAL, log );
            }
        } catch ( Exception e ) {
            e.printStackTrace( );
        }
    }
}
