package com.cn.contact;

/**
 * 联系人Bean
 * Created by yunzhao.liu on 2017/11/11
 */

public class ContactInfo{

    /**
     * 联系人ID
     */
    private String contactId;

    /**
     * 联系人名称的首字母
     */
    private String letter;

    /**
     * 联系人显示的名称
     */
    private String name;
    /**
     * 联系人的手机号码, 有可能是多个. 同一个联系人的不同手机号码,视为多个联系人
     */
    private String phone;

    /**
     * 是否添加联系人
     */
    private boolean isAddContact;

    /**
     * 是否选择上联系人
     */
    private boolean isChooseContact;

    public boolean isChooseContact() {
        return isChooseContact;
    }

    public void setChooseContact(boolean chooseContact) {
        isChooseContact = chooseContact;
    }

    public boolean isAddContact() {
        return isAddContact;
    }

    public void setAddContact(boolean addContact) {
        isAddContact = addContact;
    }

    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    public String getLetter() {
        return letter;
    }

    public void setLetter(String letter) {
        this.letter = letter;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
