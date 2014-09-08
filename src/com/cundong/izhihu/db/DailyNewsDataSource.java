package com.cundong.izhihu.db;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.cundong.izhihu.entity.NewsListEntity.NewsEntity;
import com.cundong.izhihu.util.GsonUtils;

public final class DailyNewsDataSource {

	private SQLiteDatabase database;
	private DBHelper dbHelper;
	private String[] allColumns = { DBHelper.COLUMN_ID, DBHelper.COLUMN_DATE,
			DBHelper.COLUMN_CONTENT };

	public DailyNewsDataSource(Context context) {
		dbHelper = new DBHelper(context);
		database = dbHelper.getWritableDatabase();
	}

	public ArrayList<NewsEntity> insertDailyNewsList(String date, String content) {
		ContentValues values = new ContentValues();
		values.put(DBHelper.COLUMN_DATE, date);
		values.put(DBHelper.COLUMN_CONTENT, content);

		long insertId = database.insert(DBHelper.TABLE_NAME, null, values);
		Cursor cursor = database.query(DBHelper.TABLE_NAME, allColumns,
				DBHelper.COLUMN_ID + " = " + insertId, null, null, null, null);
		ArrayList<NewsEntity> newsList = cursorToNewsList(cursor);
		cursor.close();
		return newsList;
	}

	public void updateNewsList(String date, String content) {
		ContentValues values = new ContentValues();
		values.put(DBHelper.COLUMN_DATE, date);
		values.put(DBHelper.COLUMN_CONTENT, content);
		database.update(DBHelper.TABLE_NAME, values, DBHelper.COLUMN_DATE
				+ "='" + date + "'", null);
	}

	public void insertOrUpdateNewsList(String date, String content) {
		if (TextUtils.isEmpty(content))
			return;

		if (isContentExist(date)) {
			updateNewsList(date, content);
		} else {
			insertDailyNewsList(date, content);
		}
	}

	private boolean isContentExist(String key) {
		
		boolean result = false;
		
		Cursor cursor = database.query(DBHelper.TABLE_NAME, allColumns,
				DBHelper.COLUMN_DATE + " = '" + key + "'", null, null, null, null);
		
		if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
			String content = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_CONTENT));
			result  = !TextUtils.isEmpty(content);
		} 
		
		cursor.close();
		return result;
	}
	
	public String getContent(String key) {
		Cursor cursor = database.query(DBHelper.TABLE_NAME, allColumns,
				DBHelper.COLUMN_DATE + " = '" + key + "'", null, null, null,
				null);

		if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
			String content = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_CONTENT));
			cursor.close();
			return content;
		} else {
			return null;
		}
	}
	
	public ArrayList<NewsEntity> getNewsList(String date) {
		Cursor cursor = database.query(DBHelper.TABLE_NAME, allColumns,
				DBHelper.COLUMN_DATE + " = '" + date + "'", null, null, null,
				null);

		ArrayList<NewsEntity> newsList = cursorToNewsList(cursor);

		cursor.close();
		return newsList;
	}

	private ArrayList<NewsEntity> cursorToNewsList(Cursor cursor) {
		if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
			String content = cursor.getString(cursor
					.getColumnIndex(DBHelper.COLUMN_CONTENT));
			return GsonUtils.getNewsList(content);
		} else {
			return null;
		}
	}
}