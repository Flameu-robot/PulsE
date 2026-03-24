package com.example.messengerservice.entity.enums;

import lombok.Data;

import java.io.Serializable;

@Data
public class GroupFeatures implements Serializable {
    private boolean voiceEnabled = false;
    private boolean rolesEnabled = false;
    private boolean videoEnabled = false;
    private boolean screenShareEnabled = false;
}