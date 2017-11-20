package com.websocketim.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.websocketim.Constants;
import com.websocketim.adapter.ContactsAdapter;
import com.websocketim.R;
import com.websocketim.manager.DialogManager;
import com.websocketim.manager.IMChatManager;
import com.websocketim.model.ChatMessage;
import com.websocketim.model.UserInfo;
import com.websocketim.utils.CommonUtil;
import com.websocketim.utils.MsgCache;
import com.websocketim.view.Sidebar;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContactListActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "ContactListActivity";

    private Sidebar sidebar;
    private ListView mListView;
    private ImageView back;

    private int sidebarId;
    private int mListViewId;
    private int backId;

    private ContactsAdapter adapter;
    private String master;
    private ChatMessage chatMessage = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            chatMessage = (ChatMessage) bundle.getSerializable("ChatMessage");
        }

        initView();
        initData();

    }

    private void initView() {
        sidebarId = R.id.sidebar;
        mListViewId = R.id.list;
        backId = R.id.back;
        sidebar = (Sidebar) findViewById(sidebarId);
        mListView = (ListView) findViewById(mListViewId);
        this.back = (ImageView) findViewById(backId);

        this.back.setOnClickListener(this);

    }

    private void initData() {

        UserInfo userinfo = (UserInfo) MsgCache.get(ContactListActivity.this).getAsObject(Constants.USER_INFO);
        if (!CommonUtil.isBlank(userinfo)) {
            master = userinfo.getUsername();
        } else {
            finish();
        }

        final List<UserInfo> contactList = loadLocalContact();
        adapter = new ContactsAdapter(getApplication(), contactList);

        View footerView = LayoutInflater.from(getApplication()).inflate(R.layout.item_contact_list_footer, null);
        View headView = LayoutInflater.from(getApplication()).inflate(R.layout.item_contact_list_header, null);
        TextView tv_total = footerView.findViewById(R.id.tv_total);

        if (contactList.size() > 0) {
            tv_total.setText(contactList.size() + "位联系人");
        }
        mListView.addHeaderView(headView);
        mListView.addFooterView(footerView);
        mListView.setAdapter(adapter);
        sidebar.setListView(mListView);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final UserInfo user = contactList.get(i - 1);
                if (chatMessage != null) {
                    DialogManager.showDialog(ContactListActivity.this, "\n发送该消息给" + user.getUsername(), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            sendMessageToFriend(user,chatMessage);
                            finish();
                        }
                    },null,null);
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putString("master", master);
                    bundle.putString("username", user.getUsername());
                    bundle.putString("nickname", user.getNickname());
                    bundle.putString("avatar", "http://userimage3.360doc.com/13/0624/20/12890102_201306242009350436_main.jpg");
                    Intent intent = new Intent(ContactListActivity.this, UserInfoPreviewActivity.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            }
        });
    }

    private void sendMessageToFriend(UserInfo user,ChatMessage chatMessage) {
        ChatMessage cm = new ChatMessage();
        cm.setMaster(master);
        cm.setFromuser(master);
        cm.setFromusernick(master);
        cm.setTouser(user.getUsername());
        cm.setTousernick(user.getNickname());
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        cm.setTime(df.format(new Date()));
        String clientId = CommonUtil.getStringToDate(cm.getTime()) + (int) (Math.random() * 10);
        cm.setClientId(clientId);
        cm.setUsername(master);
        cm.setNickname(master);
        cm.setMsgState("read");
        cm.setPrompt("true");
        cm.setType(chatMessage.getType());
        cm.setContentType(chatMessage.getContentType());
        cm.setContent(chatMessage.getContent());
        cm.setUrl(chatMessage.getUrl());
        cm.setExtra(chatMessage.getExtra());
        cm.setThumbnailUrl(chatMessage.getThumbnailUrl());
        Intent intent = new Intent(Constants.SEND_CHATMESSAGE);
        Bundle bundle = new Bundle();
        bundle.putSerializable("ChatMessage", cm);
        intent.putExtras(bundle);
        sendBroadcast(intent);
        IMChatManager.getInstance(getApplicationContext()).save(cm);
//                    if (chatMessage.getContentType().equals(ChatMessage.CHAT_CONTENT_TYPE_TXT)) {
//
//                    } else if (chatMessage.getContentType().equals(ChatMessage.CHAT_CONTENT_TYPE_AUDIO)) {
//
//                    } else if (chatMessage.getContentType().equals(ChatMessage.CHAT_CONTENT_TYPE_PIC)) {
//
//                    } else if (chatMessage.getContentType().equals(ChatMessage.CHAT_CONTENT_TYPE_VIDEO)) {
//
//                    }
    }

    private List<UserInfo> loadLocalContact() {

        List<UserInfo> localContact = new ArrayList<UserInfo>();
        String[] sections = new String[]{"↑", "☆", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "#"};
//        for (int i = 0; i < sections.length; i++) {
//            Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER}, null, null, null);
//            while (phones.moveToNext()) {
//                String title = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
//                String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
//                phoneNumber = getFormatPhone(phoneNumber);
//
//                UserInfo user = new UserInfo();
//                user.setMaster(title);
//                user.setAvatar(phoneNumber);
//                CommonUtil.setUserInitialLetter(user);
//                if (isPhoneNumber(phoneNumber) && sections[i].equals(user.getInitialLetter())) {
//                    localContact.add(user);
//                }
//            }
//            phones.close();
//        }
        String name[] = new String[]{"赵秀萍", "王文杰", "付凤兰", "苏云霞", "边春玲", "王欣", "苏敏萍"};
        String id[] = new String[]{"4827", "4834", "4851", "4870", "4872", "4877", "4954"};
        for (int j = 0; j < sections.length; j++) {
            for (int i = 0; i < name.length; i++) {
                UserInfo user = new UserInfo();
                user.setNickname(name[i]);
                user.setUsername(id[i]);
                CommonUtil.setUserInitialLetter(user);
                if (sections[j].equals(user.getInitialLetter())) {
                    localContact.add(user);
                }
            }
        }
        return localContact;
    }

    private String getFormatPhone(String phone) {
        StringBuffer sb = new StringBuffer();
        phone = phone.replace("-", "");
        phone = phone.replace(" ", "");
        Pattern p1 = Pattern.compile("^((\\+{0,1}86){0,1})1[0-9]{10}");
        Matcher m1 = p1.matcher(phone);
        if (m1.matches()) {
            Pattern p2 = Pattern.compile("^((\\+{0,1}86){0,1})");
            Matcher m2 = p2.matcher(phone);
            while (m2.find()) {
                m2.appendReplacement(sb, "");
            }
            m2.appendTail(sb);
        }
        return sb.toString();
    }

    @Override
    public void onClick(View view) {
        finish();
    }
}
