package com.cn.lyz.contactlist;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.cn.contact.ContactInfo;
import com.cn.contact.ContactsUtils;
import com.cn.contact.GlideCircleTransform;

import java.util.ArrayList;

/**
 * 联系人Adapter
 * Created by yunzhao.liu on 2017/11/11
 */

public class ContactAdapter1 extends RecyclerView.Adapter<ContactAdapter1.MyViewHodle> {

    private Context context;
    private ArrayList<ContactInfo> mContactList;
    private int chooseModel;

    public ContactAdapter1(Context context, ArrayList<ContactInfo> list, int chooseModel) {
        this.context = context;
        this.mContactList = list;
        this.chooseModel = chooseModel;
    }

    @Override
    public ContactAdapter1.MyViewHodle onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHodle(LayoutInflater.from(context).inflate(com.cn.contact.R.layout.item_contacts_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(ContactAdapter1.MyViewHodle holder, final int position) {
        ContactInfo contactInfo = mContactList.get(position);
        holder.name.setText(contactInfo.getName());
        holder.phone.setText(contactInfo.getPhone());

        //判断是否显示索引字母
        String currentLetter = contactInfo.getLetter();
        String previousLetter = position >= 1 ? mContactList.get(position - 1).getLetter() : "";
        if (!TextUtils.equals(currentLetter, previousLetter)) {
            holder.letter.setVisibility(View.VISIBLE);
            holder.letter.setText(currentLetter);
        } else {
            holder.letter.setVisibility(View.GONE);
        }

        holder.cb.setVisibility(View.GONE);
        holder.add.setVisibility(View.GONE);

        //加载联系人头像
        Glide.with(context)
                .load(ContactsUtils.getPhotoByte(context, contactInfo.getContactId()))
                .transform(new GlideCircleTransform(context))
                .placeholder(com.cn.contact.R.mipmap.ic_launcher)
                .error(com.cn.contact.R.mipmap.ic_launcher)
                .into(holder.iv);
    }

    @Override
    public int getItemCount() {
        return mContactList == null ? 0 : mContactList.size();
    }

    public void setContactList(ArrayList<ContactInfo> contactList) {
        mContactList = contactList;
        notifyDataSetChanged();
    }

    /**
     * 刷新数据
     */
    public void notifyRefreshData() {
        notifyDataSetChanged();
    }

    public class MyViewHodle extends RecyclerView.ViewHolder {

        private LinearLayout itemLayout;
        private TextView letter;
        private ImageView iv;
        private CheckBox cb;
        private TextView name;
        private TextView phone;
        private TextView add;

        public MyViewHodle(View itemView) {
            super(itemView);
            itemLayout = itemView.findViewById(com.cn.contact.R.id.item_layout);
            letter = itemView.findViewById(com.cn.contact.R.id.letter);
            cb = itemView.findViewById(com.cn.contact.R.id.cb);
            add = itemView.findViewById(com.cn.contact.R.id.add);
            iv = itemView.findViewById(com.cn.contact.R.id.iv);
            name = itemView.findViewById(com.cn.contact.R.id.name);
            phone = itemView.findViewById(com.cn.contact.R.id.phone);
        }
    }
}
