package com.example.jinbailiang.albumdemo_jin;

/**
 * GridView��ÿ��item�����ݶ���
 *
 * @author len
 *
 */
public class ImageBean{
	/**
	 * �ļ��еĵ�һ��ͼƬ·��
	 */
	private String topImagePath;
	/**
	 * �ļ�����
	 */
	private String folderName;
	/**
	 * �ļ����е�ͼƬ��
	 */
	private int imageCounts;

	public ImageBean() {
		// TODO Auto-generated constructor stub
	}

	public ImageBean(String topImagePath,String folderName, int imageCounts) {
		this.topImagePath = topImagePath;
		this.folderName = folderName;
		this.imageCounts = imageCounts;
	}

	public String getTopImagePath() {
		return topImagePath;
	}
	public void setTopImagePath(String topImagePath) {
		this.topImagePath = topImagePath;
	}
	public String getFolderName() {
		return folderName;
	}
	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}
	public int getImageCounts() {
		return imageCounts;
	}
	public void setImageCounts(int imageCounts) {
		this.imageCounts = imageCounts;
	}

}
