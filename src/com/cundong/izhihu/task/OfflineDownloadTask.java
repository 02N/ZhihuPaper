package com.cundong.izhihu.task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Context;
import android.text.TextUtils;

import com.cundong.izhihu.Constants;
import com.cundong.izhihu.ZhihuApplication;
import com.cundong.izhihu.entity.NewsDetailEntity;
import com.cundong.izhihu.entity.NewsListEntity;
import com.cundong.izhihu.entity.NewsListEntity.NewsEntity;
import com.cundong.izhihu.http.HttpClientUtils;
import com.cundong.izhihu.util.GsonUtils;
import com.cundong.izhihu.util.Logger;
import com.cundong.izhihu.util.MD5Util;
import com.cundong.izhihu.util.SDCardUtils;
import com.cundong.izhihu.util.StreamUtils;

public class OfflineDownloadTask extends BaseGetNewsTask {

	private Context mContext = null;
	
	public OfflineDownloadTask(ResponseListener listener) {
		super(listener);
	}
	
	public OfflineDownloadTask(Context context, ResponseListener listener) {
		super(listener);
		
		mContext = context;
	}
	
	@Override
	protected String doInBackground(String... params) {

		String content = null;

		try {
			content = getUrl(Constants.Url.URL_LATEST);
			
			NewsListEntity newsListEntity = (NewsListEntity)GsonUtils.getEntity(content, NewsListEntity.class);
			ArrayList<NewsEntity> stories = newsListEntity!=null ? newsListEntity.stories : null;
			
			if (stories != null && stories.size() != 0) {

				for (NewsEntity newsEntity : stories) {
					String detailContent = getUrl(Constants.Url.URL_DETAIL + newsEntity.id);
					
					ZhihuApplication.getDataSource().insertOrUpdateNewsList("detail_" + newsEntity.id, detailContent);
					
					NewsDetailEntity detailEntity = (NewsDetailEntity) GsonUtils.getEntity(
							content, NewsDetailEntity.class);
					
					if (detailEntity == null || TextUtils.isEmpty(detailEntity.body)) {
						continue;
					}
					
					ArrayList<String> imageList = new ArrayList<String>();
					imageList.add(detailEntity.image);
					imageList.addAll(getImages(detailEntity.body));
					
					File file = null;
					for (String imageUrl : imageList) {
						
						if (TextUtils.isEmpty(imageUrl)) {
							Logger.getLogger().e(
									"no download, the image url is null");
							continue;
						}
						
						String fileName = MD5Util.encrypt(imageUrl);

						String filePath = SDCardUtils.getExternalCacheDir(mContext) + fileName + ".jpg";
						
						file = new File(filePath);
						if(!file.exists()) {
							try {
								file.createNewFile();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						
						InputStream in = null;
						OutputStream out = null;

						// from web
						try {
							in = HttpClientUtils.request(mContext, imageUrl, null);
							out = new FileOutputStream(file);

							StreamUtils.copy(in, out);
							
						} catch (IOException e) {
							e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							StreamUtils.close(out);
							StreamUtils.close(in);
						}
						
					}
					
				}
				
				return "success";
			}
		} catch (IOException e) {
			e.printStackTrace();
			
			isRefreshSuccess = false;
		}

		return null;
	}
	
	private ArrayList<String> getImages(String html) {

		ArrayList<String> imgList = new ArrayList<String>();

		Document doc = Jsoup.parse(html);

		Elements es = doc.getElementsByTag("img");
		
		for (Element e : es) {
			String src = e.attr("src");
			
			String newImgUrl = src.replaceAll("\"", "");
			newImgUrl = newImgUrl.replace('\\', ' ');
			newImgUrl = newImgUrl.replaceAll(" ", "");
			
			imgList.add( newImgUrl );
		}
		
		return imgList;
	}
}