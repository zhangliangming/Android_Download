package com.zlm.test;

import java.util.Date;

import com.zlm.download.manage.DownloadManage;
import com.zlm.download.manage.DownloadThreadManage;
import com.zlm.download.model.DownloadTask;
import com.zlm.download.util.DateUtil;
import com.zlm.download.util.DownloadThreadPool;
import com.zlm.download.util.IDGenerateUtil;
import com.zlm.download.util.DownloadThreadPool.IDownloadTaskFinishCallBack;
import com.zlm.download.util.DownloadThreadPool.IDownloadThreadEventCallBack;

/**
 * android模拟器
 * 
 * @author zhangliangming
 * 
 */
public class MainAPP {

	private static IDownloadThreadEventCallBack downloadThreadEventCallBack = new IDownloadThreadEventCallBack() {

		@Override
		public void waitingCancel(DownloadTask task) {
			System.out.println("任务" + task.getTid() + "等待取消");
		}

		@Override
		public void waiting(DownloadTask task) {
			System.out.println("任务" + task.getTid() + "正在等待");
		}

		@Override
		public void threadDownloading(DownloadTask task, int threadID,
				int threadCount, int downloadedSize) {

		}

		@Override
		public void pauseed(DownloadTask task, int downloadedSize) {
			System.out.println("任务" + task.getTid() + "暂停下载，当前下载进度为："
					+ downloadedSize);
		}

		@Override
		public void finished(DownloadTask task) {
			System.out.println("任务" + task.getTid() + "完成下载");
		}

		@Override
		public void error(DownloadTask task) {
			System.out.println("任务" + task.getTid() + "下载出错");
		}

		@Override
		public void downloading(DownloadTask task, int downloadedSize) {
			System.out.println("任务" + task.getTid() + "正在下载，当前下载进度为："
					+ downloadedSize);
		}

		@Override
		public void canceled(DownloadTask task) {
			System.out.println("任务" + task.getTid() + "取消下载");
		}
	};

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String sid = "14650076545080000";
		downloadSong(sid);

		 try {
		 Thread.sleep(500);
		 } catch (InterruptedException e) {
		 e.printStackTrace();
		 }
		 String sid2 = "14650077103150001";
		 downloadSong(sid2);
		
		 try {
		 Thread.sleep(500);
		 } catch (InterruptedException e) {
		 e.printStackTrace();
		 }
		 String sid3 = "14650078173690002";
		 downloadSong(sid3);
	}

	private static void downloadSong(String sid) {
		DownloadTask task = new DownloadTask();
		String url = "http://192.168.0.102:8080/HappyPlayer/phone/getSongInfoDataByID?sid="
				+ sid;
		String tid = sid;
		task.setTid(tid);
		task.setDownloadUrl(url);
		task.setFilePath("D:/" + tid + ".mp3");
		task.setAddTime(DateUtil.dateToYMDHMSString(new Date()));
		task.setFinishTime("");
		task.setType(DownloadTask.SONG_DOWNLOAD);

		DownloadThreadManage dtm = new DownloadThreadManage(task, 1, 1000);
		task.setDownloadThreadManage(dtm);
		DownloadThreadPool dp = DownloadManage.getImageTM();
		dp.setDownloadThreadEventCallBack(downloadThreadEventCallBack);

		dp.addDownloadTaskByAddTime(task);

	}

}
