package com.miniit.rechargeapp.services;

import android.accessibilityservice.AccessibilityService;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import com.miniit.rechargeapp.interfaces.USSDResponseInterface;
import java.util.ArrayList;
import java.util.List;

public class USSDService extends AccessibilityService {

    private static String TAG = "MINIIT";
    private static AccessibilityEvent event;
    static USSDResponseInterface ussdResponseInterface;

    public static void setUssdResponseInterface(USSDResponseInterface u) {
        ussdResponseInterface = u;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        this.event = event;

        Log.d(TAG, "onAccessibilityEvent");

        Log.d(TAG, String.format(
                "onAccessibilityEvent: [type] %s [class] %s [package] %s [time] %s [text] %s",
                event.getEventType(), event.getClassName(), event.getPackageName(),
                event.getEventTime(), event.getText()));

        ussdResponseInterface.onResponse(event.getText().toString());

        if (USSDController.instance == null || !USSDController.instance.isRunning) {
            return;
        }

        if (LoginView(event) && notInputText(event)) {
            clickOnButton(event, 0);
            USSDController.instance.isRunning = false;
            USSDController.instance.callbackInvoke.over(event.getText().get(0).toString());
        } else if (problemView(event) || LoginView(event)) {
            clickOnButton(event, 1);
            USSDController.instance.callbackInvoke.over(event.getText().get(0).toString());
        } else if (isUSSDWidget(event)) {
            String response = event.getText().get(0).toString();
            if (response.contains("\n")) {
                response = response.substring(response.indexOf('\n') + 1);
            }
            if (notInputText(event)) {
                clickOnButton(event, 0);
                USSDController.instance.isRunning = false;
                USSDController.instance.callbackInvoke.over(response);
            } else {
                // sent option 1
                if (USSDController.instance.callbackInvoke != null) {
                    USSDController.instance.callbackInvoke.responseInvoke(response);
                    USSDController.instance.callbackInvoke = null;
                } else {
                    USSDController.instance.callbackMessage.responseMessage(response);
                }
            }
        }

    }

    public static void send(String text) {
        setTextIntoField(event, text);
        clickOnButton(event, 1);
    }

    public static void cancel() {
        clickOnButton(event, 0);
    }

    private static void setTextIntoField(AccessibilityEvent event, String data) {
        USSDController ussdController = USSDController.instance;
        Bundle arguments = new Bundle();
        arguments.putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, data);

        for (AccessibilityNodeInfo leaf : getLeaves(event)) {
            if (leaf.getClassName().equals("android.widget.EditText")
                    && !leaf.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)) {
                ClipboardManager clipboardManager = ((ClipboardManager) ussdController.context
                        .getSystemService(Context.CLIPBOARD_SERVICE));
                if (clipboardManager != null) {
                    clipboardManager.setPrimaryClip(ClipData.newPlainText("text", data));
                }

                leaf.performAction(AccessibilityNodeInfo.ACTION_PASTE);
            }
        }
    }

    protected static boolean notInputText(AccessibilityEvent event) {
        boolean flag = true;
        for (AccessibilityNodeInfo leaf : getLeaves(event)) {
            if (leaf.getClassName().equals("android.widget.EditText")) flag = false;
        }
        return flag;
    }
    private boolean isUSSDWidget(AccessibilityEvent event) {
        return (event.getClassName().equals("amigo.app.AmigoAlertDialog")
                || event.getClassName().equals("android.app.AlertDialog"));
    }

    private boolean LoginView(AccessibilityEvent event) {
        return isUSSDWidget(event)
                && USSDController.instance.map.get(USSDController.KEY_LOGIN)
                .contains(event.getText().get(0).toString());
    }

    protected boolean problemView(AccessibilityEvent event) {
        return isUSSDWidget(event)
                && USSDController.instance.map.get(USSDController.KEY_ERROR)
                .contains(event.getText().get(0).toString());
    }

    protected static void clickOnButton(AccessibilityEvent event, int index) {
        int count = -1;
        for (AccessibilityNodeInfo leaf : getLeaves(event)) {
            if (leaf.getClassName().toString().toLowerCase().contains("button")) {
                count++;
                if (count == index) {
                    leaf.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            }
        }
    }

    private static List<AccessibilityNodeInfo> getLeaves(AccessibilityEvent event) {
        List<AccessibilityNodeInfo> leaves = new ArrayList<>();
        if (event.getSource() != null) {
            getLeaves(leaves, event.getSource());
        }

        return leaves;
    }

    private static void getLeaves(List<AccessibilityNodeInfo> leaves, AccessibilityNodeInfo node) {
        if (node.getChildCount() == 0) {
            leaves.add(node);
            return;
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            getLeaves(leaves, node.getChild(i));
        }
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "onInterrupt");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "onServiceConnected");
    }
}