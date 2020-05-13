package com.miniit.rechargeapp.interfaces;

import com.miniit.rechargeapp.services.USSDController;

import java.util.HashMap;
import java.util.HashSet;

public interface USSDApi {
    void send(String text, USSDController.CallbackMessage callbackMessage);
    void cancel();
    void callUSSDInvoke(String ussdPhoneNumber, HashMap<String, HashSet<String>> map,
                        USSDController.CallbackInvoke callbackInvoke);
    void callUSSDInvoke(String ussdPhoneNumber, int simSlot, HashMap<String, HashSet<String>> map,
                        USSDController.CallbackInvoke callbackInvoke);
    void callUSSDOverlayInvoke(String ussdPhoneNumber, HashMap<String, HashSet<String>> map,
                               USSDController.CallbackInvoke callbackInvoke);
    void callUSSDOverlayInvoke(String ussdPhoneNumber, int simSlot, HashMap<String, HashSet<String>> map,
                               USSDController.CallbackInvoke callbackInvoke);
}
