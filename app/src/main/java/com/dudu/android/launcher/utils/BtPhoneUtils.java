package com.dudu.android.launcher.utils;

import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;

import com.dudu.aios.ui.bt.Contact;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Chad on 2016/4/9.
 */
public class BtPhoneUtils {
    /**
     * 蓝牙电话状态
     */
    public static int btCallState = -1;//默认设置-1为初始状态
    /**
     * 判断是否需要重新加载蓝牙电话通讯录
     */
    public static boolean isNeedLoadContacts = false;

    /**
     * 拨出的电话来源
     */
    public static int btCallOutSource = -1;
    public static final int BTCALL_OUT_SOURCE_DEFAULT = -1;
    /**
     * 手机端拨出电话
     */
    public static final int BTCALL_OUT_SOURCE_MOBILE = 1;
    /**
     * 后视镜键盘拨出电话
     */
    public static final int BTCALL_OUT_SOURCE_KEYBOARD = 2;
    /**
     * 语音呼叫号码拨出电话
     */
    public static final int BTCALL_OUT_SOURCE_VOIC = 3;

    /**
     * 蓝牙连接状态
     */
    public static int connectionState = -1;
    /**
     * The profile is in disconnected state
     */
    public static final int STATE_DISCONNECTED = 0;
    /**
     * The profile is in connecting state
     */
    public static final int STATE_CONNECTING = 1;
    /**
     * The profile is in connected state
     */
    public static final int STATE_CONNECTED = 2;
    /**
     * The profile is in disconnecting state
     */
    public static final int STATE_DISCONNECTING = 3;


    /* Call state */
    /**
     * Call is active.
     */
    public static final int CALL_STATE_ACTIVE = 0;
    /**
     * Call is in held state.
     */
    public static final int CALL_STATE_HELD = 1;
    /**
     * Outgoing call that is being dialed right now.
     */
    public static final int CALL_STATE_DIALING = 2;
    /**
     * Outgoing call that remote party has already been alerted about.
     */
    public static final int CALL_STATE_ALERTING = 3;
    /**
     * Incoming call that can be accepted or rejected.
     */
    public static final int CALL_STATE_INCOMING = 4;
    /**
     * Waiting call state when there is already an active call.
     */
    public static final int CALL_STATE_WAITING = 5;
    /**
     * Call that has been held by response and hold
     * (see Bluetooth specification for further references).
     */
    public static final int CALL_STATE_HELD_BY_RESPONSE_AND_HOLD = 6;
    /**
     * Call that has been already terminated and should not be referenced as a valid call.
     */
    public static final int CALL_STATE_TERMINATED = 7;

    /**
     * Use a simple string represents the long.
     */
    private static final String COLUMN_CONTACT_ID =
            ContactsContract.Data.CONTACT_ID;
    private static final String COLUMN_RAW_CONTACT_ID =
            ContactsContract.Data.RAW_CONTACT_ID;
    private static final String COLUMN_MIMETYPE =
            ContactsContract.Data.MIMETYPE;
    private static final String COLUMN_NAME =
            ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME;
    private static final String COLUMN_NUMBER =
            ContactsContract.CommonDataKinds.Phone.NUMBER;
    private static final String COLUMN_NUMBER_TYPE =
            ContactsContract.CommonDataKinds.Phone.TYPE;
    private static final String MIMETYPE_STRING_NAME =
            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE;
    private static final String MIMETYPE_STRING_PHONE =
            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE;
    public static final String WLD_NAME = "wld_name";
    public static final String WLD_TYPE = "wld_type";
//    private Logger logger = LoggerFactory.getLogger("phone.BtPhoneUtils");



    /**
     * @param context
     * @param name The contact who you getDefaultConfig the id from. The name of
     * the contact should be set.
     * @return 0 if contact not exist in contacts list. Otherwise return
     * the id of the contact.
     */
    public static String getContactID(Context context, String name) {
        String id = "0";
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(
                android.provider.ContactsContract.Contacts.CONTENT_URI,
                new String[]{android.provider.ContactsContract.Contacts._ID},
                android.provider.ContactsContract.Contacts.DISPLAY_NAME +
                        "='" + name + "'", null, null);
        if (cursor.moveToNext()) {
            id = cursor.getString(cursor.getColumnIndex(
                    android.provider.ContactsContract.Contacts._ID));
        }
        if (null != cursor) {
            cursor.close();
        }
        return id;
    }

    /**
     * You must specify the contact's ID.
     *
     * @param contact
     * @throws Exception The contact's name should not be empty.
     */
    public static void addContact(Context context, Contact contact) {
//        Log.w(TAG, "**add start**");
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        ContentResolver resolver = context.getContentResolver();
        String id = getContactID(context, contact.getName());
        if (!id.equals("0")) {
//            Log.d(TAG, "contact already exist. exit.");
        } else if (contact.getName().trim().equals("")) {
//            Log.d(TAG, "contact name is empty. exit.");
        } else {
            ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build());
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(COLUMN_RAW_CONTACT_ID, 0)
                    .withValue(COLUMN_MIMETYPE, MIMETYPE_STRING_NAME)
                    .withValue(COLUMN_NAME, contact.getName())
                    .build());
//            Log.d(TAG, "add name: " + contact.getName());

            if (!contact.getNumber().trim().equals("")) {
                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(COLUMN_RAW_CONTACT_ID, 0)
                        .withValue(COLUMN_MIMETYPE, MIMETYPE_STRING_PHONE)
                        .withValue(COLUMN_NUMBER, contact.getNumber())
//                        .withValue(COLUMN_NUMBER_TYPE, contact.getNumberType())
                        .build());
//                Log.d(TAG, "add number: " + contact.getNumber());
            }

            try {
                resolver.applyBatch(ContactsContract.AUTHORITY, ops);
                Log.v("BtPhoneUtils", "add success");
            } catch (Exception e) {
                Log.e("BtPhoneUtils", "add failed");
                Log.e("BtPhoneUtils", e.getMessage());
            }
        }

    }
    /**
     * 更新联系人信息
     * */
    /**
     * @param contactOld The contact wants to be updated. The name should exists.
     * @param contactNew
     */
    public static void updateContact(Context context, Contact contactOld, Contact contactNew) {
//        logger.trace("**update start**");
        ContentResolver resolver = context.getContentResolver();
        String id = getContactID(context, contactOld.getName());
        if (id.equals("0")) {
//            logger.trace(contactOld.getName()+" not exist.");
        } else if (contactNew.getName().trim().equals("")) {
//            logger.trace( "contact name is empty. exit.");
        } else if (!getContactID(context, contactNew.getName()).equals("0")) {
//            logger.trace( "new contact name already exist. exit.");
        } else {

            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

            //update name
            ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(COLUMN_CONTACT_ID + "=? AND " + COLUMN_MIMETYPE + "=?",
                            new String[]{id, MIMETYPE_STRING_NAME})
                    .withValue(COLUMN_NAME, contactNew.getName())
                    .build());
//            logger.trace( "update name: " + contactNew.getName());

            //update number
            if (!contactNew.getNumber().trim().equals("")) {
                ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                        .withSelection(COLUMN_CONTACT_ID + "=? AND " + COLUMN_MIMETYPE + "=?",
                                new String[]{id, MIMETYPE_STRING_PHONE})
                        .withValue(COLUMN_NUMBER, contactNew.getNumber())
//                        .withValue(COLUMN_NUMBER_TYPE, contactNew.getNumberType())
                        .build());
//                logger.trace("update number: " + contactNew.getNumber());
            }

            try {
                resolver.applyBatch(ContactsContract.AUTHORITY, ops);
                Log.v("BtPhoneUtils", "update success");
            } catch (Exception e) {
                Log.e("BtPhoneUtils", "update failed");
                Log.e("BtPhoneUtils", e.getMessage());
            }
        }
//        logger.trace( "**update end**");
    }

    /**
     * 查询指定电话的联系人姓名
     */
    public static String queryContactNameByNumber(Context context, final String phoneNum) throws Exception {
        if (null == phoneNum || "".equals(phoneNum)) {
            return "";
        }
        String name = "";
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, phoneNum); //根据电话号码查找联系人
//        Uri uri = Uri.parse("content://com.android.contacts/data/phones/filter/" + phoneNum);

        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(uri, new String[]{"display_name"}, null, null, null);
        if (cursor.moveToFirst()) {
            name = cursor.getString(0);
        }
        cursor.close();
        return name;
    }

    /**
     * 查询获取指定姓名的所有联系人
     *
     * @param context
     * @return
     */
    public static List<Contact> obtainContactsByName(Context context, String name_key) {
        List<Contact> contacts = new ArrayList<>();
        if (null == context || TextUtils.isEmpty(name_key)) {
            return contacts;
        }
        Cursor cursor = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
                null, ContactsContract.Contacts.DISPLAY_NAME + " = ?",
                new String[]{name_key + ""}, ContactsContract.Contacts.DISPLAY_NAME + " desc");
        int contactIdIndex = 0;
        int nameIndex = 0;

        //chad modified begin
        if (null == cursor) {
            return contacts;
        }//chad modified end

        if (cursor.getCount() > 0) {
            contactIdIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID);
            nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
        }
        while (cursor.moveToNext()) {
            String contactId = cursor.getString(contactIdIndex);
            String name = cursor.getString(nameIndex);
            if (name.equals(name_key)) {

                Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contactId,
                        null, null);
                int phoneIndex = 0;
                if (phones.getCount() > 0) {
                    phoneIndex = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                }
                while (phones.moveToNext()) {
                    String phoneNumber = phones.getString(phoneIndex);
                    Contact contact = new Contact();
                    contact.setId(Integer.parseInt(contactId));
                    contact.setName(name);
                    contact.setNumber(phoneNumber);
                    contacts.add(contact);
                }
                if (null != phones) {
                    phones.close();
                }
            }
        }
        if (null != cursor) {
            cursor.close();
        }
        return contacts;
    }

    /**
     * 查询获取所有联系人
     *
     * @param context
     * @return
     */
    public static List<Contact> obtainContacts(Context context) {
        if (null == context) {
            return null;
        }
        List<Contact> contacts = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, ContactsContract.Contacts.DISPLAY_NAME + " desc");
        int contactIdIndex = 0;
        int nameIndex = 0;

        //chad modified begin
        if (null == cursor) {
            return contacts;
        }//chad modified end

        if (cursor.getCount() > 0) {
            contactIdIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID);
            nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
        }
        while (cursor.moveToNext()) {
            String contactId = cursor.getString(contactIdIndex);
            String name = cursor.getString(nameIndex);
            Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contactId,
                    null, null);
            int phoneIndex = 0;
            if(null!=phones){

                if (phones.getCount() > 0) {
                    phoneIndex = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                }
                while (phones.moveToNext()) {

                    String phoneNumber = phones.getString(phoneIndex).replace("-","").replace(" ","");
                    Contact contact = new Contact();
                    contact.setId(Integer.parseInt(contactId));
                    contact.setName(name);
                    contact.setNumber(phoneNumber);
                    contacts.add(contact);
                }
                phones.close();
            }
        }
        if (null != cursor) {
            cursor.close();
        }
        return contacts;
    }

    /**
     * 清除通讯录记录
     */
    public static void doClearContacts(Context context) {

        ContentResolver resolver = context.getContentResolver();
        String where = ContactsContract.RawContacts.ACCOUNT_NAME + "= ?"
                + " AND " + ContactsContract.RawContacts.ACCOUNT_TYPE + "=?";
        resolver.delete(ContactsContract.RawContacts.CONTENT_URI, where,
                new String[]{WLD_NAME, WLD_TYPE});
    }

    public static void deleteContacts(Context context){
        ContentResolver resolver = context.getContentResolver();
        resolver.delete(ContactsContract.RawContacts.CONTENT_URI.buildUpon().appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER,"true").build(), null, null);
    }

    /**
     * 设置蓝牙电话音量
     * @param value
     */
    public static void setBtPhoneVolume(Context context, int type, int value){
        //STREAM_BLUETOOTH_SCO -- 蓝牙通话
        AudioManager audiomanager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if(null!=audiomanager){
            int currentVolume = audiomanager.getStreamVolume(Constants.STREAM_BLUETOOTH_SCO/*AudioManager.STREAM_BLUETOOTH_SCO*/); // 获取当前值
            audiomanager.setParameters("hfp_volume="+currentVolume);
            audiomanager.setStreamVolume(type, value, 0);
        }
    }


    /**
     * 获取蓝牙电话最大音量
     * @param context
     * @return
     */
    public static int getBtPhoneMaxVolume(Context context, int type){
        //STREAM_BLUETOOTH_SCO -- 蓝牙通话
        AudioManager audiomanager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = 0 ;
        if(null!=audiomanager) {
            maxVolume  = audiomanager.getStreamMaxVolume(type/*AudioManager.STREAM_BLUETOOTH_SCO*/); // 获取当前值
        }
        return maxVolume;
    }/**
     * 获取蓝牙电话当前音量
     * @param context
     * @return
     */
    public static int getBtPhoneCurrentVolume(Context context){
        //STREAM_BLUETOOTH_SCO -- 蓝牙通话
        AudioManager audiomanager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int currentVolume = 0 ;
        if(null!=audiomanager) {
            currentVolume  = audiomanager.getStreamVolume(Constants.STREAM_BLUETOOTH_SCO/*AudioManager.STREAM_BLUETOOTH_SCO*/); // 获取当前值
        }
        return currentVolume;
    }

    public static void initAudio(Context context){
        //STREAM_BLUETOOTH_SCO -- 蓝牙通话
        AudioManager audiomanager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        //设置声音模式
        audiomanager.setMode(AudioManager.MODE_NORMAL);
        //打开麦克风
        audiomanager.setMicrophoneMute(false);
        //打开扬声器
        audiomanager.setSpeakerphoneOn(true);
    }
    /**
     * 与设备配对 参考源码：platform/packages/apps/Settings.git
     * \Settings\src\com\android\settings\bluetooth\CachedBluetoothDevice.java
     */
    public static boolean createBond(Class btClass, BluetoothDevice btDevice) throws Exception {
        Method createBondMethod = btClass.getMethod("createBond");
        Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }

    /**
     * 与设备解除配对 参考源码：platform/packages/apps/Settings.git
     * \Settings\src\com\android\settings\bluetooth\CachedBluetoothDevice.java
     */
    public static boolean removeBond(Class btClass, BluetoothDevice btDevice) throws Exception {
        Method removeBondMethod = btClass.getMethod("removeBond");
        Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }

    public static boolean setPin(Class btClass, BluetoothDevice btDevice, String str) throws Exception {
        try {
            Method removeBondMethod = btClass.getDeclaredMethod("setPin", new Class[]{byte[].class});
            Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice, new Object[]{str.getBytes()});
            Log.e("returnValue", "" + returnValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;

    }

    /**
     * 功能：取消用户输入
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static boolean cancelPairingUserInput(Class btClass, BluetoothDevice device) throws Exception {
        Method createBondMethod = btClass.getMethod("cancelPairingUserInput");
        // cancelBondProcess()
        Boolean returnValue = (Boolean) createBondMethod.invoke(device);
        return returnValue.booleanValue();
    }

    /**
     * 功能：取消配对
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static boolean cancelBondProcess(Class btClass, BluetoothDevice device) throws Exception {
        Method createBondMethod = btClass.getMethod("cancelBondProcess");
        Boolean returnValue = (Boolean) createBondMethod.invoke(device);
        return returnValue.booleanValue();
    }

    /**
     * 修改蓝牙设备名称
     * @param name
     * @return
     */
    public static boolean setBluetoothDeviceName(String name){
        if(TextUtils.isEmpty(name)){
            return false;
        }
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        return adapter.setName(name);
    }
    /**
     * 设置蓝牙可见性
     *
     * @param timeout
     */
    public static void setDiscoverableTimeout(int timeout) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        try {
            Method setDiscoverableTimeout = BluetoothAdapter.class.getMethod("setDiscoverableTimeout", int.class);
            setDiscoverableTimeout.setAccessible(true);
            Method setScanMode = BluetoothAdapter.class.getMethod("setScanMode", int.class, int.class);
            setScanMode.setAccessible(true);

            setDiscoverableTimeout.invoke(adapter, timeout);
            setScanMode.invoke(adapter, BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE, timeout);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据包名关掉进程
     *
     * @param packageName
     */
    public static void killProcessByName(Context context, String packageName) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        am.killBackgroundProcesses(packageName);  //应用的包名
    }

    /**
     * 判断某个服务是否正在运行的方法
     *
     * @param mContext
     * @param serviceName 是包名+服务的类名（例如：net.loonggg.testbackstage.TestService）
     * @return true代表正在运行，false代表服务没有正在运行
     */
    public static boolean isServiceWork(Context mContext, String serviceName) {
        boolean isWork = false;
        ActivityManager myAM = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> myList = myAM.getRunningServices(40);
        if (myList.size() <= 0) {
            return false;
        }
        for (int i = 0; i < myList.size(); i++) {
            String mName = myList.get(i).service.getClassName().toString();
            if (mName.equals(serviceName)) {
                isWork = true;
                break;
            }
        }
        return isWork;
    }
}
