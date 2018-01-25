package com.cn.contact;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;

import com.github.promeg.pinyinhelper.Pinyin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.database.Cursor.FIELD_TYPE_STRING;

/**
 * 联系人工具类
 * Created by yunzhao.liu on 2017/11/11
 */

public class ContactsUtils {

    private static Map<String, byte[]> photoMap = new HashMap<>();


    /**
     * 异步返回联系人列表
     */
    public static ArrayList<ContactInfo> getContactList(Context context, ArrayList<ContactInfo> addContacts) {
        final ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, new String[]{"_id"}, null, null, null);
        ArrayList<ContactInfo> contactsInfos = new ArrayList<>();
        if (cursor != null && cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                do {
                    int contactIdIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID);//获取 id 所在列的索引
                    String contactId = cursor.getString(contactIdIndex);//联系人id

                    final List<String> phones = getData1(contentResolver, contactId, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);//联系人电话(可能包含多个)
                    //电话为空时，不添加到列表中
                    if (phones.isEmpty()) {
                        continue;
                    } else {
                        String name;
                        final List<String> names = getData1(contentResolver, contactId, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);//联系人名称
                        if (names.isEmpty()) {
                            name = phones.get(0);
                        } else {
                            name = names.get(0);
                        }
                        //相同联系人的不同手机号视为不同联系人
                        for (String phone : phones) {
                            Log.d("phone=", phone);
                            ContactInfo info = new ContactInfo();
                            info.setContactId(contactId);
                            info.setName(name);
                            info.setPhone(phone);
                            String letter = String.valueOf(Pinyin.toPinyin(name.charAt(0)).toUpperCase().charAt(0));
                            //非字母开头的统一设置成 "#"
                            if (isLetter(letter)) {
                                info.setLetter(letter);
                            } else {
                                info.setLetter("#");
                            }

                            /**
                             * 用已添加的联系人和所有联系人比较，姓名和电话都相等则标记为已添加
                             */
                            if (addContacts != null && addContacts.size() > 0) {
                                for (ContactInfo model : addContacts) {
                                    if (model.getName().equals(info.getName())) {
                                        //替换手机号前中后空格
                                        if (model.getPhone().replaceAll("\\s*", "")
                                                .equals(info.getPhone().replaceAll("\\s*", ""))) {
                                            info.setAddContact(true);
                                        }
                                    }
                                }
                            }

                            contactsInfos.add(info);
                        }
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        compare(contactsInfos);
        return contactsInfos;
    }

    /**
     * 把联系人按照a b c升序排列
     */
    private static ArrayList<ContactInfo> compare(ArrayList<ContactInfo> contactInfos) {
        Collections.sort(contactInfos, new Comparator<ContactInfo>() {
            @Override
            public int compare(ContactInfo o1, ContactInfo o2) {
                //升序排列
                if (o1.getLetter().equals("@")
                        || o2.getLetter().equals("#")) {
                    return -1;
                } else if (o1.getLetter().equals("#")
                        || o2.getLetter().equals("@")) {
                    return 1;
                }
                return o1.getLetter().compareTo(o2.getLetter());
            }
        });
        return contactInfos;
    }

    /**
     * 判断字符是否是字母
     */
    public static boolean isLetter(String s) {
        char c = s.charAt(0);
        int i = (int) c;
        if ((i >= 65 && i <= 90) || (i >= 97 && i <= 122)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 根据MIMETYPE类型, 返回对应联系人data1字段的数据
     */
    private static List<String> getData1(final ContentResolver contentResolver, String contactId, final String mimeType) {
        List<String> dataList = new ArrayList<>();

        Cursor dataCursor = contentResolver.query(ContactsContract.Data.CONTENT_URI,
                new String[]{ContactsContract.Data.DATA1},
                ContactsContract.Data.CONTACT_ID + "=?" + " AND "
                        + ContactsContract.Data.MIMETYPE + "='" + mimeType + "'",
                new String[]{String.valueOf(contactId)}, null);
        if (dataCursor != null && dataCursor.getCount() > 0) {
            if (dataCursor.moveToFirst()) {
                do {
                    final int columnIndex = dataCursor.getColumnIndex(ContactsContract.Data.DATA1);
                    final int type = dataCursor.getType(columnIndex);
                    if (type == FIELD_TYPE_STRING) {
                        final String data = dataCursor.getString(columnIndex);
                        if (!TextUtils.isEmpty(data)) {
                            dataList.add(data);
                        }
                    }
                } while (dataCursor.moveToNext());
            }
            dataCursor.close();
        }
        return dataList;
    }



    /**
     * 获取联系人图片
     */
    public static byte[] getPhotoByte(final Context context, String contactId) {
        byte[] bytes = photoMap.get(contactId);
        if (bytes == null || bytes.length <= 0) {
            Cursor dataCursor = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI,
                    new String[]{"data15"},
                    ContactsContract.Data.CONTACT_ID + "=?" + " AND "
                            + ContactsContract.Data.MIMETYPE + "='" + ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE + "'",
                    new String[]{String.valueOf(contactId)}, null);
            if (dataCursor != null) {
                if (dataCursor.getCount() > 0) {
                    dataCursor.moveToFirst();
                    bytes = dataCursor.getBlob(dataCursor.getColumnIndex("data15"));
                    photoMap.put(contactId, bytes);
                }
                dataCursor.close();
            }
        }
        return bytes;
    }

    /**
     * 模糊搜索（按中文，数字，字母搜索）
     */
    public static boolean searchContact(String searchStr, ContactInfo info) {
        return info.getName().contains(searchStr) || info.getPhone().contains(searchStr)
                || searchLowerByAlphabet(searchStr, info) || searchUpperByAlphabet(searchStr, info)
                || Pinyin.toPinyin(info.getName(), "").toLowerCase().contains(searchStr)
                || Pinyin.toPinyin(info.getName(), "").toUpperCase().contains(searchStr);
    }

    /**
     * 按中文首字母
     * 如“中国人”可以搜 “zgr”
     */
    private static boolean searchLowerByAlphabet(String searchStr, ContactInfo info) {
        String[] temp = Pinyin.toPinyin(info.getName(), ",").toLowerCase().split(",");
        StringBuilder builder = new StringBuilder();
        for (String str : temp) {
            builder.append(str.charAt(0));
        }
        if (builder.toString().contains(searchStr)) {
            return true;
        }
        return false;
    }

    /**
     * 按中文首字母
     * 如“中国人”可以搜 “ZGR”
     */
    private static boolean searchUpperByAlphabet(String searchStr, ContactInfo info) {
        String[] temp = Pinyin.toPinyin(info.getName(), ",").toUpperCase().split(",");
        StringBuilder builder = new StringBuilder();
        for (String str : temp) {
            builder.append(str.charAt(0));
        }
        if (builder.toString().contains(searchStr)) {
            return true;
        }
        return false;
    }
}
