package com.axotsoft.wicket.realm;

import com.axotsoft.wicket.bluetooth.ConnectionRecord;
import com.axotsoft.wicket.devices.DeviceRecord;
import com.axotsoft.wicket.widget.WidgetRecord;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmAsyncTask;
import io.realm.RealmConfiguration;

public class Database {

    private static final int DEFAULT_CONNECTION_ID = 0;
    private Realm realm;

    public Database() {
        this(Realm.getInstance((new RealmConfiguration.Builder()).name("devices").build()));
    }

    public Database(Realm realm) {
        this.realm = realm;
    }

    public DeviceRecord getDevice(String address) {
        return realm.where(DeviceRecord.class).equalTo(DeviceRecord.FIELD_ADDRESS, address).findFirst();
    }

    public ConnectionRecord getConnection() {
        return realm.where(ConnectionRecord.class).equalTo(ConnectionRecord.FIELD_ID, DEFAULT_CONNECTION_ID).findFirst();
    }

    public ConnectionRecord createConnectionRecord(DeviceRecord deviceRecord) {
        ConnectionRecord connectionRecord = new ConnectionRecord(DEFAULT_CONNECTION_ID, deviceRecord);
        execute(realm1 -> realm1.insert(connectionRecord));
        return getConnection();
    }

    public WidgetRecord getWidget(int id) {
        return realm.where(WidgetRecord.class).equalTo(WidgetRecord.FIELD_WIDGET_ID, id).findFirst();
    }

    public List<DeviceRecord> getAllDevices() {
        return realm.where(DeviceRecord.class).findAll();
    }

    public void execute(Realm.Transaction transaction) {
        realm.executeTransaction(transaction);
    }

    public RealmAsyncTask executeAsync(Realm.Transaction transaction) {
        return realm.executeTransactionAsync(transaction);
    }

    public RealmAsyncTask executeAsync(Realm.Transaction transaction, Realm.Transaction.OnSuccess onSuccess) {
        return realm.executeTransactionAsync(transaction, onSuccess);
    }
}
