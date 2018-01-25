package com.cn.contact;

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
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

/**
 * 联系人Adapter
 * Created by yunzhao.liu on 2017/11/11
 */

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.MyViewHodle> {

    private Context context;
    private ArrayList<ContactInfo> mContactList;
    private int chooseModel;

    public ContactAdapter(Context context, ArrayList<ContactInfo> list, int chooseModel) {
        this.context = context;
        this.mContactList = list;
        this.chooseModel = chooseModel;
    }

    @Override
    public ContactAdapter.MyViewHodle onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHodle(LayoutInflater.from(context).inflate(R.layout.item_contacts_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(ContactAdapter.MyViewHodle holder, final int position) {
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

        //是否选择上
        boolean isChooseContact = contactInfo.isChooseContact();
        holder.cb.setChecked(isChooseContact);

        //是否已添加及选择模式
        boolean isAddContact = contactInfo.isAddContact();
        if (chooseModel == ChooseModel.MODEL_MULTI) {
            if (isAddContact) {
                holder.cb.setVisibility(View.GONE);
                holder.add.setVisibility(View.VISIBLE);
            } else {
                holder.cb.setVisibility(View.VISIBLE);
                holder.add.setVisibility(View.GONE);
            }
        } else {
            holder.cb.setVisibility(View.GONE);
            if (isAddContact) {
                holder.add.setVisibility(View.VISIBLE);
            } else {
                holder.add.setVisibility(View.GONE);
            }
        }

        //加载联系人头像
        Glide.with(context)
                .load(ContactsUtils.getPhotoByte(context, contactInfo.getContactId()))
                .transform(new GlideCircleTransform(context))
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .into(holder.iv);

        //条目点击事件
        holder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mContactList.get(position).isAddContact()) {
                    Toast.makeText(context, "已添加", Toast.LENGTH_SHORT).show();
                    return;
                }
                mOnItemClickListener.onItemClick(mContactList.get(position), position);
            }
        });
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
            itemLayout = itemView.findViewById(R.id.item_layout);
            letter = itemView.findViewById(R.id.letter);
            cb = itemView.findViewById(R.id.cb);
            add = itemView.findViewById(R.id.add);
            iv = itemView.findViewById(R.id.iv);
            name = itemView.findViewById(R.id.name);
            phone = itemView.findViewById(R.id.phone);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(ContactInfo info, int position);
    }

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }
}
