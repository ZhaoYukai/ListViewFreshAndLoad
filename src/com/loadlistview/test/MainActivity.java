package com.loadlistview.test;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

import com.loadlistview.test.R;

public class MainActivity extends Activity implements ILoadListener , IReflashListener{
	
	ArrayList<ApkEntity> apk_list = new ArrayList<ApkEntity>();
	MyAdapter adapter;
	LoadListView listview;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		getData();
		showListView(apk_list);
	}

	
	private void showListView(ArrayList<ApkEntity> apk_list) {
		if (adapter == null) {
			listview = (LoadListView) findViewById(R.id.listview);
			listview.setInterface(MainActivity.this , MainActivity.this);
			adapter = new MyAdapter(this, apk_list);
			listview.setAdapter(adapter);
		} else {
			adapter.onDateChange(apk_list);
		}
	}
	
	

	private void getData() {
		for (int i = 0; i < 10; i++) {
			ApkEntity entity = new ApkEntity();
			entity.setName("测试程序");
			entity.setInfo("50w用户");
			entity.setDes("这是一个神奇的应用！");
			apk_list.add(entity);
		}
	}
	
	
	private void getLoadData() {
		for (int i = 0; i < 2; i++) {
			ApkEntity entity = new ApkEntity();
			entity.setName("更多-----程序");
			entity.setInfo("50w用户");
			entity.setDes("这是一个神奇的应用！");
			apk_list.add(entity);
		}
	}

	
	
	
	@Override
	public void onLoad() {
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				//获取更多数据
				getLoadData();
				//更新listview显示
				showListView(apk_list);
				//通知listview加载完毕
				listview.loadComplete();
			}
		}, 2000);
	}
	
	
	
	private void getReflashData() {
		for (int i = 0; i < 4; i++) {
			ApkEntity entity = new ApkEntity();
			entity.setName("刷新-----程序");
			entity.setInfo("50w用户");
			entity.setDes("这是一个神奇的应用！");
			apk_list.add(0 , entity);
		}
	}


	@Override
	public void onReflash() {
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				//获取最新数据
				getReflashData();
				//通知界面显示
				showListView(apk_list);
				//通知listview刷新数据完毕
				listview.reflashComplete();
			}
		}, 2000);
	}
	
	
	
	
	
}
