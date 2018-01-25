package com.cn.lyz.contactlist;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cn.contact.ChooseModel;
import com.cn.contact.ContactAdapter;
import com.cn.contact.ContactInfo;
import com.cn.contact.ContactsUtils;
import com.cn.contact.SideLetterBar;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 联系人选择
 * Created by yunzhao.liu on 2017/11/11
 */

public class ContactListActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private SideLetterBar mSideLetterBar;
    private ProgressBar mProgressBar;
    private ArrayList<ContactInfo> mContactList;
    private TextView mOverlay;
    private EditText mSearchBox;
    private ImageView mClearBtn;

    private ArrayList<ContactInfo> mSearchList;
    private ContactAdapter mContactAdapter;
    private CheckBox mChoose;
    private Button mOk;
    private RelativeLayout mBottomRoot;
    private ArrayList<ContactInfo> mAddContact;
    private ArrayList<ContactInfo> mChooseContacts;

    private int chooseModel = ChooseModel.MODEL_SINGLE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);
        initView();
        initData();
        initClick();
    }

    private void initView() {
        mSearchBox = (EditText) findViewById(R.id.et_search);
        mClearBtn = (ImageView) findViewById(R.id.iv_search_clear);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mSideLetterBar = (SideLetterBar) findViewById(R.id.side_bar);
        mOverlay = (TextView) findViewById(R.id.letter_overlay);
        mSideLetterBar.setOverlay(mOverlay);
        mProgressBar = (ProgressBar) findViewById(R.id.pb);

        mBottomRoot = (RelativeLayout) findViewById(R.id.bottom_root);
        mChoose = (CheckBox) findViewById(R.id.choose);
        mOk = (Button) findViewById(R.id.ok);
    }

    private void initClick() {
        mSideLetterBar.setOnLetterChangedListener(new SideLetterBar.OnLetterChangedListener() {
            @Override
            public void onLetterChanged(String letter) {
                scrollToLetter(letter);
            }
        });

        mClearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchBox.setText("");
                mClearBtn.setVisibility(View.GONE);
            }
        });

        mSearchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String searchKey = s.toString().trim();
                if (mContactList.size() == 0) return;

                mSearchList.clear();
                searchContacts(searchKey);
                if (TextUtils.isEmpty(searchKey)) {
                    mClearBtn.setVisibility(View.GONE);
                    mSideLetterBar.setVisibility(View.VISIBLE);
                    mSearchList.clear();
                    mContactAdapter.setContactList(mContactList);
                } else {
                    mClearBtn.setVisibility(View.VISIBLE);
                    mContactAdapter.setContactList(mSearchList);
                    if (mSearchList == null || mSearchList.size() <= 0) {
                        mSideLetterBar.setVisibility(View.GONE);
                    } else {
                        mSideLetterBar.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        //联系人未获取到之前搜索不可用
        mSearchBox.setEnabled(false);

        mContactAdapter.setOnItemClickListener(new ContactAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ContactInfo info, int position) {
                //单选模式
                if (chooseModel == ChooseModel.MODEL_SINGLE) {
                    mChooseContacts = new ArrayList<>();
                    mChooseContacts.add(info);
                    ContactEvent contactEvent = new ContactEvent();
                    contactEvent.setContactInfos(mChooseContacts);
                    EventBus.getDefault().post(contactEvent);
                    finish();
                } else {
                    //多选模式
                    if (mContactList != null && mContactList.size() > 0) {
                        for (ContactInfo model : mContactList) {
                            if (model.getName().equals(info.getName())) {
                                //替换手机号前中后空格
                                if (model.getPhone().replaceAll("\\s*", "")
                                        .equals(info.getPhone().replaceAll("\\s*", ""))) {
                                    //修改是否选择上联系人
                                    if (info.isChooseContact()) {
                                        model.setChooseContact(false);
                                    } else {
                                        model.setChooseContact(true);
                                    }
                                }
                            }
                        }
                    }

                    //通知刷新
                    mContactAdapter.notifyRefreshData();

                    //判断是否全部选择
                    boolean tempChoose = true;
                    for (int i = 0; i < mContactList.size(); i++) {
                        if (!mContactList.get(i).isChooseContact()) {
                            chooseChange(mChoose, false);
                            tempChoose = false;
                            break;
                        }
                    }
                    if (tempChoose) {
                        chooseChange(mChoose, true);
                    }
                }
            }
        });

        mChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //当正在加载时，不处理点击事件
                if (mProgressBar.getVisibility() == View.VISIBLE) {
                    return;
                }

                if (mChoose.isChecked()) {
                    allChoose();
                } else {
                    cancelChoose();
                }
            }
        });

        mOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取到选中的联系人
                mChooseContacts = new ArrayList<>();
                for (int i = 0; i < mContactList.size(); i++) {
                    if (mContactList.get(i).isChooseContact()
                            && !mContactList.get(i).isAddContact()) {
                        mChooseContacts.add(mContactList.get(i));
                    }
                }

                //通过EventBus发送给调用者
                if (mChooseContacts.size() > 0) {
                    ContactEvent contactEvent = new ContactEvent();
                    contactEvent.setContactInfos(mChooseContacts);
                    EventBus.getDefault().post(contactEvent);
                }

                finish();
            }
        });


    }

    /**
     * 全选、取消全选
     */
    private void chooseChange(CheckBox view, boolean checked) {
        if (checked) {
            view.setText("取消全选");
            mChoose.setChecked(true);
        } else {
            view.setText("全选");
            mChoose.setChecked(false);
        }
    }

    /**
     * 选择全部联系人
     */
    private void allChoose() {
        for (int i = 0; i < mContactList.size(); i++) {
            mContactList.get(i).setChooseContact(true);
        }
        chooseChange(mChoose, true);
        mContactAdapter.notifyRefreshData();
    }

    /**
     * 取消所有联系人
     */
    private void cancelChoose() {
        for (int i = 0; i < mContactList.size(); i++) {
            mContactList.get(i).setChooseContact(false);
        }
        chooseChange(mChoose, false);
        mContactAdapter.notifyRefreshData();
    }

    /**
     * 搜索联系人
     *
     * @param searchKey 搜索key
     */
    private void searchContacts(String searchKey) {
        for (ContactInfo info : mContactList) {
            if (ContactsUtils.searchContact(searchKey, info)) {
                mSearchList.add(info);
            }
        }
    }

    /**
     * 滑动到索引字母出
     */
    private void scrollToLetter(String letter) {
        for (int i = 0; i < mContactList.size(); i++) {
            if (TextUtils.equals(letter, mContactList.get(i).getLetter())) {
                ((LinearLayoutManager) mRecyclerView.getLayoutManager()).scrollToPositionWithOffset(i, 0);
                break;
            }
        }
    }

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            mProgressBar.setVisibility(View.GONE);

            if (mContactList.size() > 0) {
                mSearchBox.setEnabled(true);
                if (chooseModel == ChooseModel.MODEL_MULTI) {
                    mBottomRoot.setVisibility(View.VISIBLE);
                }
                mContactAdapter.setContactList(mContactList);
                mSideLetterBar.setVisibility(View.VISIBLE);
            }
            return true;
        }
    });

    /**
     * 获取已经添加过的联系人
     */
    private ArrayList<ContactInfo> getAddContact() {
        ArrayList<ContactInfo> list = new ArrayList<>();
        ContactInfo info;
        ContactInfo info1;
        info = new ContactInfo();
        info.setAddContact(true);
        info.setChooseContact(true);
        info.setName("啊");
        info.setPhone("13112345677");
        list.add(info);

        info1 = new ContactInfo();
        info1.setAddContact(true);
        info1.setChooseContact(true);
        info1.setName("本机");
        info1.setPhone("18836524194");
        list.add(info1);
        return list;
    }

    private void initData() {
        Intent intent = getIntent();
        chooseModel = intent.getIntExtra(ChooseModel.CHOOSEMODEL, ChooseModel.MODEL_SINGLE);
        mBottomRoot.setVisibility(View.GONE);

        mAddContact = getAddContact();
        mSearchList = new ArrayList<>();
        mContactList = new ArrayList<>();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mContactAdapter = new ContactAdapter(this, mContactList, chooseModel);
        mRecyclerView.setAdapter(mContactAdapter);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                mContactList = ContactsUtils.getContactList(ContactListActivity.this, mAddContact);
                handler.sendEmptyMessage(0);
            }
        });
    }
}
