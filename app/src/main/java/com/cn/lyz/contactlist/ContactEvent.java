package com.cn.lyz.contactlist;


import com.cn.contact.ContactInfo;

import java.util.ArrayList;

/**
 * EventBus事件类，用于传递List
 * Created by yunzhao.liu on 2017/11/11
 */

public class ContactEvent {
    public ArrayList<ContactInfo> mContactInfos;

    public ContactEvent() {
    }

    public ContactEvent(ArrayList<ContactInfo> infos) {
        this.mContactInfos = infos;
    }

    public ArrayList<ContactInfo> getContactInfos() {
        return mContactInfos;
    }

    public void setContactInfos(ArrayList<ContactInfo> contactInfos) {
        mContactInfos = contactInfos;
    }
}
