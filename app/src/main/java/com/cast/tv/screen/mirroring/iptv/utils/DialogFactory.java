package com.cast.tv.screen.mirroring.iptv.utils;

import android.app.Dialog;
import android.content.Context;
import android.widget.Button;

import com.cast.tv.screen.mirroring.iptv.R;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class DialogFactory {
    public static SweetAlertDialog getDialogError(Context context, String message) {
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                .setTitleText(context.getString(R.string.header_error_text))
                .setContentText(message)
                .setConfirmClickListener(Dialog::dismiss)
                .showCancelButton(false)
                .setConfirmText(context.getString(R.string.confirm_text));

        sweetAlertDialog.setOnShowListener(dialog -> {
            setColorButton(context, sweetAlertDialog);
        });

        return sweetAlertDialog;
    }

    public static SweetAlertDialog getDialogConfirm(Context context, String message) {
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                .setTitleText(context.getString(R.string.confirm_message))
                .setContentText(message)
                .setConfirmText(context.getString(R.string.yes_text))
                .setCancelText(context.getString(R.string.no_text));

        sweetAlertDialog.setCancelable(false);

        sweetAlertDialog.setOnShowListener(dialog -> {
            setColorButton(context, sweetAlertDialog);
        });

        return sweetAlertDialog;
    }

    public static SweetAlertDialog getDialogSuccess(Context context, String header, String message) {
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText(header)
                .setContentText(message)
                .showCancelButton(false)
                .setConfirmText(context.getString(R.string.confirm_text))
                .setConfirmClickListener(Dialog::dismiss);

        sweetAlertDialog.setOnShowListener(dialog -> {
            setColorButton(context, sweetAlertDialog);
        });
        return sweetAlertDialog;
    }

    public static SweetAlertDialog getDialogErrorAndRetry(Context context, String title, String message) {
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                .setTitleText(title)
                .setContentText(message)
                .setConfirmText(context.getString(R.string.retry_text))
                .setCancelText(context.getString(R.string.exit_text));
        sweetAlertDialog.setCancelable(false);
        sweetAlertDialog.setOnShowListener(dialog -> {
            setColorButton(context, sweetAlertDialog);
        });
        return sweetAlertDialog;
    }

    public static SweetAlertDialog getDialogDoSomething(Context context, String title, String message) {
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                .setTitleText(title)
                .setContentText(message)
                .setConfirmText(context.getString(R.string.confirm_text))
                .setCancelText(context.getString(R.string.cancel_text));
        sweetAlertDialog.setOnShowListener(dialog -> {
            setColorButton(context, sweetAlertDialog);
        });
        return sweetAlertDialog;
    }

    public static SweetAlertDialog getDialogRequestSomething(Context context, String title, String message) {
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                .setTitleText(title)
                .setContentText(message)
                .setConfirmText(context.getString(R.string.agree_text))
                .setCancelText(context.getString(R.string.not_now_text));
        sweetAlertDialog.setCancelable(false);
        sweetAlertDialog.setOnShowListener(dialog -> {
            setColorButton(context, sweetAlertDialog);
        });
        return sweetAlertDialog;
    }

    public static SweetAlertDialog getDialogRequestRating(Context context, String title, String message) {
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(context, SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                .setCustomImage(R.mipmap.ic_launcher)
                .setTitleText(title)
                .setContentText(message)
                .setConfirmText(context.getString(R.string.agree_text));

        sweetAlertDialog.setCancelable(true);
        sweetAlertDialog.setCanceledOnTouchOutside(true);
        sweetAlertDialog.setOnShowListener(dialog -> {
            setColorButton(context, sweetAlertDialog);
        });
        return sweetAlertDialog;
    }

    //         sweetAlertDialog.getProgressHelper().setProgress(0.6f);
    public static SweetAlertDialog getDialogProgress(Context context, String message) {
        SweetAlertDialog progressDialog = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
        progressDialog.getProgressHelper().setBarColor(ColorUtils.getColorFromResource(context, R.color.jade_theme_darker_color));
        progressDialog.setContentText(context.getString(R.string.please_wait_text));
        progressDialog.showCancelButton(false);
        progressDialog.setTitleText(message);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.setOnShowListener(dialog -> {
            setColorButton(context, progressDialog);
        });
        return progressDialog;
    }

    public static SweetAlertDialog getDialogNotice(Context context, String message) {
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(context, SweetAlertDialog.NORMAL_TYPE)
                .setTitleText(context.getString(R.string.guide_text))
                .setContentText(message)
                .showCancelButton(false)
                .setConfirmText(context.getString(R.string.understood_text));
        sweetAlertDialog.setCancelable(true);
        sweetAlertDialog.setOnShowListener(dialog -> {
            setColorButton(context, sweetAlertDialog);
        });
        return sweetAlertDialog;
    }

    public static void setColorButton(Context context, SweetAlertDialog sweetAlertDialog) {
        Button confirmButton = sweetAlertDialog.getButton(SweetAlertDialog.BUTTON_CONFIRM);
        Button cancelButton = sweetAlertDialog.getButton(SweetAlertDialog.BUTTON_CANCEL);
        if (confirmButton != null) {
            confirmButton.setBackground(context.getDrawable(R.drawable.dialog_confirm_button));
        }

        if (cancelButton != null) {
            cancelButton.setBackground(context.getDrawable(R.drawable.dialog_cancel_button));
        }
    }
}
