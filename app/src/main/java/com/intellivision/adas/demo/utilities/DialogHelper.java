package com.intellivision.adas.demo.utilities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.View;
import android.widget.Button;

/**
 * This class is used to display dialog.
 * 
 */
public class DialogHelper {
    private static AlertDialog.Builder _builder;
    private static AlertDialog _dialog;

    /**
     * Method to display dialog.
     * 
     * @param activity
     *            the activity context
     * @param title
     *            the string resource id of dialog title
     * @param mesage
     *            the string resource id of dialog message
     * @param btnNegative
     *            the string resource id of dialog negative button
     * @param btnNeutral
     *            the string resource id of dialog neutral button
     * @param btnPositive
     *            the string resource id of dialog positive button
     * @param onClickListener
     *            the {@link OnClickListener} to handle dialog button click events.
     */
    public static void showDialog( final Activity activity, final int title, final int mesage, final int btnNegative, final int btnNeutral, final int btnPositive,
            final OnClickListener onClickListener ) {
        activity.runOnUiThread( new Runnable( ) {
            @Override
            public void run() {
                try {
                    if ( _dialog != null && _dialog.isShowing( ) ) {
                        _dialog.dismiss( );
                    }

                    _builder = new AlertDialog.Builder( activity );
                    _builder.setTitle( title );
                    _builder.setMessage( mesage );
                    if ( btnNegative != -1 ) {
                        _builder.setNegativeButton( btnNegative, onClickListener );
                    }

                    if ( btnNeutral != -1 ) {
                        _builder.setNeutralButton( btnNeutral, onClickListener );
                    }

                    if ( btnPositive != -1 ) {
                        _builder.setPositiveButton( btnPositive, onClickListener );
                    }

                    _builder.setCancelable( false );

                    _dialog = _builder.create( );
                    _dialog.setCanceledOnTouchOutside( false );
                    _dialog.show( );
                } catch ( Exception e ) {
                    e.printStackTrace( );
                }
            }
        } );
    }

    /**
     * Method to display dialog.
     * 
     * @param activity
     *            the activity context
     * @param title
     *            the string resource id of dialog title
     * @param mesage
     *            the string resource id of dialog message
     * @param btnNegative
     *            the string resource id of dialog negative button
     * @param btnNeutral
     *            the string resource id of dialog neutral button
     * @param btnPositive
     *            the string resource id of dialog positive button
     * @param onClickListener
     *            the {@link View.OnClickListener} to handle dialog button click events.
     */
    public static void showDialog( final Activity activity, final int title, final int mesage, final int btnNegative, final int btnNeutral, final int btnPositive,
            final View.OnClickListener onClickListener ) {
        activity.runOnUiThread( new Runnable( ) {
            @Override
            public void run() {
                try {
                    if ( _dialog != null && _dialog.isShowing( ) ) {
                        _dialog.dismiss( );
                    }

                    _builder = new AlertDialog.Builder( activity );
                    _builder.setTitle( title );
                    _builder.setMessage( mesage );
                    if ( btnNegative != -1 ) {
                        _builder.setNegativeButton( btnNegative, null );
                    }

                    if ( btnNeutral != -1 ) {
                        _builder.setNeutralButton( btnNeutral, null );
                    }

                    if ( btnPositive != -1 ) {
                        _builder.setPositiveButton( btnPositive, null );
                    }

                    _builder.setCancelable( false );

                    _dialog = _builder.create( );
                    _dialog.setCanceledOnTouchOutside( false );
                    _dialog.show( );

                    if ( btnNegative != -1 ) {
                        Button dlgBtn = _dialog.getButton( DialogInterface.BUTTON_NEGATIVE );
                        dlgBtn.setId( DialogInterface.BUTTON_NEGATIVE );
                        dlgBtn.setOnClickListener( onClickListener );
                    }

                    if ( btnNeutral != -1 ) {
                        Button dlgBtn = _dialog.getButton( DialogInterface.BUTTON_NEUTRAL );
                        dlgBtn.setId( DialogInterface.BUTTON_NEUTRAL );
                        dlgBtn.setOnClickListener( onClickListener );
                    }

                    if ( btnPositive != -1 ) {
                        Button dlgBtn = _dialog.getButton( DialogInterface.BUTTON_POSITIVE );
                        dlgBtn.setId( DialogInterface.BUTTON_POSITIVE );
                        dlgBtn.setOnClickListener( onClickListener );
                    }

                } catch ( Exception e ) {
                    e.printStackTrace( );
                }
            }
        } );
    }

    public static void dismissDialog() {
        try {
            if ( _dialog != null && _dialog.isShowing( ) ) {
                _dialog.dismiss( );
            }
        } catch ( Exception e ) {
            e.printStackTrace( );
        }
    }
}
