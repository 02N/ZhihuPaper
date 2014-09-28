package com.cundong.izhihu.fragment;

import java.util.ArrayList;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock.PullToRefreshLayout;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.cundong.izhihu.R;
import com.cundong.izhihu.ZhihuApplication;
import com.cundong.izhihu.activity.NewsDetailActivity;
import com.cundong.izhihu.adapter.NewsAdapter;
import com.cundong.izhihu.entity.NewsListEntity.NewsEntity;
import com.cundong.izhihu.task.GetNewsTask;
import com.cundong.izhihu.task.MyAsyncTask;
import com.cundong.izhihu.task.ResponseListener;
import com.cundong.izhihu.util.GsonUtils;

public class NewsListFragment extends BaseFragment implements ResponseListener, OnItemClickListener {

	private static final String LATEST_NEWS = "latestNews";
	
	private ListView mListView;
	private ProgressBar mProgressBar;
	private NewsAdapter mAdapter = null;
	
	private ArrayList<NewsEntity> mNewsList = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		new LoadCacheNewsTask().executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR, LATEST_NEWS);
		
		new GetNewsTask(this).executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR, LATEST_NEWS);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_main, container, false);

		mPullToRefreshLayout = (PullToRefreshLayout) view.findViewById(R.id.ptr_layout);
		mListView = (ListView) view.findViewById(R.id.list);
		mListView.setOnItemClickListener(this);
		mProgressBar = (ProgressBar) view.findViewById(R.id.progress);
		return view;
	}

	private void setListShown(boolean isListViewShown){
		mListView.setVisibility( isListViewShown ? View.VISIBLE : View.GONE);
		mProgressBar.setVisibility( isListViewShown ? View.GONE : View.VISIBLE);
	}
	
	private class LoadCacheNewsTask extends MyAsyncTask<String, Void, ArrayList<NewsEntity>> {

		@Override
		protected ArrayList<NewsEntity> doInBackground(String... params) {
			return ZhihuApplication.getDataSource().getNewsList(params[0]);
		}

		@Override
		protected void onPostExecute(ArrayList<NewsEntity> result) {
			super.onPostExecute(result);
			
			if (isAdded()) {
				if (result != null && !result.isEmpty()) {
					
					mNewsList = result;
					mAdapter = new NewsAdapter(getActivity(), mNewsList);
					mListView.setAdapter(mAdapter);
				} 
			}
		}
	}
	
	@Override
	public void onPreExecute() {
		
	}

	@Override
	public void onPostExecute(String content, boolean isRefreshSuccess, boolean isContentSame) {
		
		if (isAdded()) {
			// Notify PullToRefreshLayout that the refresh has finished
			mPullToRefreshLayout.setRefreshComplete();
			
			if (getView() != null) {
				// Show the list again
				setListShown(true);
			}
			
	        if (isRefreshSuccess) {
	            if (!isContentSame) {
	            	mNewsList = GsonUtils.getNewsList(content);
	            	
					if (mAdapter != null) {
						mAdapter.updateData(mNewsList);
					} else {
						mAdapter = new NewsAdapter(getActivity(), mNewsList);
						mListView.setAdapter(mAdapter);
					}
	            }
	        }
		}
	}

	@Override
	public void onFail(Exception e) {
		
		dealException(e);
	}

	@Override
	protected void doRefresh() {
		
		// Hide the list
		setListShown( mNewsList==null ||mNewsList.isEmpty() ? false : true );
		
		new GetNewsTask(this).executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR, LATEST_NEWS);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		
		NewsEntity newsEntity = mNewsList!=null ? mNewsList.get(position) : null;
		
		if(newsEntity==null)
			return;
		
		Intent intent = new Intent();
		intent.putExtra("id", newsEntity.id);
		intent.putExtra("newsEntity", newsEntity);
		
		intent.setClass(getActivity(), NewsDetailActivity.class);
		getActivity().startActivity(intent);
	}

	@Override
	public void onProgressUpdate(String value) {
		// TODO Auto-generated method stub
		
	}
}