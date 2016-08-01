package com.leadplatform.kfarmers.util;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.CursorLoader;
import android.util.DisplayMetrics;
import android.util.Log;

import com.leadplatform.kfarmers.view.common.ImageSelectorActivity;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import eu.medsea.mimeutil.MimeUtil;

public class ImageUtil {
	private static final String TAG = "ImageUtil";
	private static final int UNCONSTRAINED = -1;

	public static final int BITMAP_THUMBNAIL_TARGET_SIZE = 100 * 100;
	public static final int BITMAP_TARGET_SIZE = 900 * 900;
	public static final int BITMAP_TARGET_QUALITY = 90;

	/***************************************************************/
	// Gallery 에서 사진을 얻어오는 메서드
	/***************************************************************/
	public static void takePictureFromGallery(Context context, int requestCode) {
		Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		intent.setType("image/*");
		((FragmentActivity) context).startActivityForResult(intent, requestCode);
	}

	public static void takePictureFromGalleryFragment(Context context, Fragment fragment, int requestCode) {
		Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		intent.setType("image/*");
		((FragmentActivity) context).startActivityFromFragment(fragment, intent, requestCode);
	}
	
	public static void takeSelectFromGallery(Context context,int maxSize,int nowSize,String faceBookId,String faceBookDate, int requestCode) {
		Intent intent = new Intent(context, ImageSelectorActivity.class);
		intent.putExtra("maxsize", maxSize);
		intent.putExtra("nowsize", nowSize);
		intent.putExtra("faceBookId", faceBookId);
		intent.putExtra("faceBookDate", faceBookDate);
		((FragmentActivity) context).startActivityForResult(intent, requestCode);
	}
	
	public static void takeSelectGalleryFragment(Context context,int maxSize,int nowSize, Fragment fragment, int requestCode) {
		
		Intent intent = new Intent(context, ImageSelectorActivity.class);
		intent.putExtra("maxsize", maxSize);
		intent.putExtra("nowsize", nowSize);
		((FragmentActivity) context).startActivityFromFragment(fragment, intent, requestCode);
	}

	/***************************************************************/
	// Camera 에서 사진을 얻어오는 메서드
	/***************************************************************/
	public static String takePictureFromCamera(Context context, int requestCode) {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		if (intent.resolveActivity(context.getPackageManager()) != null) {
			File photoFile = createExternalStoragePublicImageFile();

			if (photoFile != null) {
				intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
				((FragmentActivity) context).startActivityForResult(intent, requestCode);
				return photoFile.getAbsolutePath();
			}
		}
		return null;
	}

	public static String takePictureFromCameraFragment(Context context, Fragment fragment, int requestCode) {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		if (intent.resolveActivity(context.getPackageManager()) != null) {
			File photoFile = createExternalStoragePublicImageFile();

			if (photoFile != null) {
				intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
				((FragmentActivity) context).startActivityFromFragment(fragment, intent, requestCode);
				return photoFile.getAbsolutePath();
			}
		}
		return null;
	}

	/***************************************************************/
	// Gallery 에서 선택한 사진의 File Path 를 리턴하는 메서드
	/***************************************************************/

    public static void copyStream(InputStream is, OutputStream os)
    {
        final int buffer_size=1024;
        try
        {
            byte[] bytes=new byte[buffer_size];
            for(;;)
            {
                int count=is.read(bytes, 0, buffer_size);
                if(count==-1)
                    break;
                os.write(bytes, 0, count);
            }
        }
        catch(Exception ex){}
    }

	public static String getConvertPathMediaStoreImageFile(Context context, Intent data) {
		String imagePath;
		Uri selectedImage = Uri.parse(Uri.decode(data.getData().toString()));
		String[] filePathColumn = { MediaStore.Images.Media.DATA };

        //getPath(context,selectedImage);

        if(isGooglePhotosUri(selectedImage))
        {
            CursorLoader cursorLoader = new CursorLoader(context, data.getData(), filePathColumn, null, null, null);
            Cursor cursor = cursorLoader.loadInBackground();
            if (cursor != null) {
                cursor.moveToFirst();
            }

            String[] str = selectedImage.getSchemeSpecificPart().toString().split("/");
            String fileName = "phpto.jpg";

            if(str != null && str.length>0) {
                 fileName = str[str.length - 1] + ".jpg";
            }

            File file = new File(ImageLoader.getInstance().getDiskCache().getDirectory().getAbsolutePath(), fileName);
            InputStream input = null;
            OutputStream output = null;
            try {
                input = context.getContentResolver().openInputStream(data.getData());
                output = new FileOutputStream(file);
                copyStream(input, output);
                output.close();
                output.close();
                input.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            cursor.close();

            return file.getPath();
        }
		else if(!selectedImage.getScheme().equals("file"))
		{
			Cursor cursor = context.getContentResolver().query(selectedImage, filePathColumn, null, null, null);
			cursor.moveToFirst();
	
			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			imagePath = cursor.getString(columnIndex);
			cursor.close();
		}
		else
		{
			imagePath = selectedImage.getPath();
		}
		return imagePath;
	}

    /*public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    *//**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     *//*
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    *//**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     *//*
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    *//**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     *//*
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    *//**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     *//*
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }*/

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {

		if("com.google.android.apps.photos.content".equals(uri.getAuthority())) {
			return true;
		} else if("com.google.android.apps.photos.contentprovider".equals(uri.getAuthority())) {
			return true;
		} else {
			return false;
		}
    }

	

	
	/*
	// 선택한 이미지 경로 가져오기.
		public final String getPath(Context context,Uri uri) {
			
			final boolean isAndroidVersionKitKat = Build.VERSION.SDK_INT >=  19; // ( == Build.VERSION_CODE.KITKAT )
			
			// Check Google Drive.
			if(isGooglePhotoUri(uri)) {
				return uri.getLastPathSegment();
			}
			
			// 1. 안드로이드 버전 체크
			// com.android.providers.media.documents/document/image :: uri로 전달 받는 경로가 킷캣으로 업데이트 되면서 변경 됨.
			if(isAndroidVersionKitKat && DocumentsContract.isDocumentUri(uri)) {
			
				//com.android.providers.media.documents/document/image:1234 ...
				//
				if(isMediaDocument(uri) && DocumentsContract.isDocumentUri(uri)) {
					final String docId = DocumentsContract.getDocumentId(uri);
		            final String[] split = docId.split(":");
		            final String type = split[0];

		            Uri contentUri = null;
		            
		            if ("image".equals(type)) {
		                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		                
		            } else if ("video".equals(type)) {
		            	return null; // 필자는 이미지만 처리할 예정이므로 비디오 및 오디오를 불러오는 부분은 작성하지 않음.
		            	
		            } else if ("audio".equals(type)) {
		            	return null;
		            }
		            
		            final String selection = Images.Media._ID + "=?";
		            final String[] selectionArgs = new String[] {
		                    split[1]
		            };

		            return getDataColumn(context, contentUri, selection, selectionArgs);
				}
				
			}
			
			// content://media/external/images/media/....
			// 안드로이드 버전에 관계없이 경로가 com.android... 형식으로 집히지 않을 수 도 있음. [ 겔럭시S4 테스트 확인 ]
			if(isPathSDCardType(uri)) {
				
				final String selection = Images.Media._ID + "=?";
	            final String[] selectionArgs = new String[] {
	                    uri.getLastPathSegment()
	            };
				
				return getDataColumn(context,  MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection, selectionArgs);
			}
			
		    // File 접근일 경우
		    else if ("file".equalsIgnoreCase(uri.getScheme())) {
		        return uri.getPath();
		    }
			
			return null;
		}



		// URI 를 받아서 Column 데이터 접근.
		public static String getDataColumn(Context context, Uri uri, String selection,
		        String[] selectionArgs) {

		    Cursor cursor = null;
		    final String column = "_data";
		    final String[] projection = {
		            column
		    };

		    try {
		    	
		        cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs ,null);
		        if (cursor != null && cursor.moveToFirst()) {
		            final int column_index = cursor.getColumnIndexOrThrow(column);
		            return cursor.getString(column_index);
		        }
		        
		    } finally {
		        if (cursor != null)
		            cursor.close();
		    }
		    
		    return null;
		}

		// 킷캣에서 추가된  document식 Path
		public static boolean isMediaDocument(Uri uri) {
			
		    return "com.android.providers.media.documents".equals(uri.getAuthority());
		}
		
		// 기본 경로 ( 킷캣 이전버전 )
		public static boolean isPathSDCardType(Uri uri) {
			// Path : external/images/media/ID(1234...)
			return "external".equals(uri.getPathSegments().get(0));
		}
		
		// 구글 드라이브를 통한 업로드 여부 체크.
		public static boolean isGooglePhotoUri(Uri uri) {
			
			return "com.google.android.apps.photos.content".equals(uri.getAuthority());
		}*/
	
	
	

	/***************************************************************/
	// 외부 저장영역에 File 을 생성하는 메서드
	/***************************************************************/
	public static File createExternalStoragePublicImageFile() {
		File image = null;
		String imageFileName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

		try {
			image = File.createTempFile(imageFileName, ".jpg", storageDir);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return image;
	}
	
	public static File createTempImageFile(File storageDir) {
		File image = null;
		String imageFileName = new SimpleDateFormat("yyyyMMdd_HHmmssss").format(new Date());
		
		try {
			image = File.createTempFile(imageFileName, ".jpg", storageDir);
			image.deleteOnExit();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return image;
	}

	/***************************************************************/
	// 외부 저장영역에 File 을 삭제하는 메서드
	/***************************************************************/
	public static void deleteExternalStoragePublicImageFile(String path) {
		File file = new File(path);
		if (file.exists())
			file.delete();
	}

	// ///////////////////////////////////////////////////////////////

	/***************************************************************/
	// EXIF정보를 회전각도로 변환하는 메서드
	/***************************************************************/
	public static int exifOrientationToDegrees(String picturePath) {
		ExifInterface exif = null;
		try {
			exif = new ExifInterface(picturePath);
			int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

			if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
				return 90;
			} else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
				return 180;
			} else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
				return 270;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/***************************************************************/
	// 이미지를 회전하는 메서드
	/***************************************************************/
	public static Bitmap rotate(Bitmap bitmap, int degrees) {
		if (degrees != 0 && bitmap != null) {
			Matrix m = new Matrix();
			m.setRotate(degrees, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);

			try {
				Bitmap converted = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
				if (bitmap != converted) {
					bitmap.recycle();
					bitmap = converted;
				}
			} catch (OutOfMemoryError ex) {
				// 메모리가 부족하여 회전을 시키지 못할 경우 그냥 원본을 반환.
				Log.e(TAG, "OutOfMemoryError error = " + ex.getMessage());
			}
		}
		return bitmap;
	}

	/***************************************************************/
	// 이미지 Size 를 축소하는 메서드
	/***************************************************************/
	public static Bitmap getSampleSizeBitmap(String picturePath, int sampleSize) {
		byte[] pictureData = getPictureByteData(picturePath);

		if (pictureData == null)
			return null;

		return getSampleSizeBitmap(pictureData, sampleSize);
	}

	public static Bitmap getSampleSizeBitmap(byte[] data, int sampleSize) {
		Bitmap bitmap = null;

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Config.RGB_565;
		options.inSampleSize = sampleSize;

		bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);

		return bitmap;
	}

	/***************************************************************/
	// 이미지 Byte Data 를 리턴하는 메서드
	/***************************************************************/
	public static byte[] getPictureByteData(String picturePath) {
		FileInputStream fileInpushStream = null;
		byte[] data = null;

		if (picturePath != null && picturePath.length() > 0) {
			try {
				File file = new File(picturePath);

				fileInpushStream = new FileInputStream(file);
				data = new byte[fileInpushStream.available()];

				while (fileInpushStream.read(data) != -1) {
                }
				fileInpushStream.close();

			} catch (FileNotFoundException e) {
				Log.e(TAG, "FileNotFoundException error = " + e.getMessage());
				data = null;
			} catch (IOException e) {
				Log.e(TAG, "IOException error = " + e.getMessage());
				data = null;
			}
		}
		return data;
	}

	public static byte[] getResourceByteData(Context context, int resouceId) {
		byte[] data = null;

		Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resouceId);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, BITMAP_TARGET_QUALITY, stream);
		data = stream.toByteArray();

		return data;
	}

	public static Bitmap loadBitmapFile(Context context, String fileName) {
		String path = getPathBitmapFile(context, fileName);

		return BitmapFactory.decodeFile(path);
	}

	public static void saveBitmapFile(Context context, String fileName, Bitmap bitmap) {
		FileOutputStream fos;
		try {
			fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
			bitmap.compress(CompressFormat.JPEG, BITMAP_TARGET_QUALITY, fos);
			fos.flush();
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void saveBitmapFile(Context context, File file, Bitmap bitmap) {
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(file);
			bitmap.compress(CompressFormat.JPEG, BITMAP_TARGET_QUALITY, fos);
			fos.flush();
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void deleteBitmapFile(Context context, String fileName) {
		if (isBitmapFileExist(context, fileName)) {
			File bitmap = new File(getPathBitmapFile(context, fileName));
			bitmap.delete();
		}
	}

	public static boolean isBitmapFileExist(Context context, String fileName) {
		boolean bRet = false;
		File filePath = new File(getPathBitmapFile(context, fileName));

		bRet = filePath.isFile();
		filePath = null;

		return bRet;
	}

	public static String getPathBitmapFile(Context context, String fileName) {
		return context.getFilesDir().toString() + "/" + fileName;
	}

	public static int computeSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
		int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);

		int roundedSize;
		if (initialSize <= 8) {
			roundedSize = 1;
			while (roundedSize < initialSize) {
				roundedSize <<= 1;
			}
		} else {
			roundedSize = (initialSize + 7) / 8 * 8;
		}

		return roundedSize;
	}

	private static int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
		double w = options.outWidth;
		double h = options.outHeight;

		int lowerBound = (maxNumOfPixels == UNCONSTRAINED) ? 1 : (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
		int upperBound = (minSideLength == UNCONSTRAINED) ? 128 : (int) Math.min(Math.floor(w / minSideLength), Math.floor(h / minSideLength));

		if (upperBound < lowerBound) {
			// return the larger one when there is no overlapping zone.
			return lowerBound;
		}

		if ((maxNumOfPixels == UNCONSTRAINED) && (minSideLength == UNCONSTRAINED)) {
			return 1;
		} else if (minSideLength == UNCONSTRAINED) {
			return lowerBound;
		} else {
			return upperBound;
		}
	}

	/*public static Bitmap makeBitmap(byte[] jpegData, int maxNumOfPixels) {
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length, options);
			if (options.mCancel || options.outWidth == -1 || options.outHeight == -1) {
				return null;
			}
			options.inSampleSize = computeSampleSize(options, UNCONSTRAINED, maxNumOfPixels);
			options.inJustDecodeBounds = false;

			options.inDither = false;
			options.inPreferredConfig = Bitmap.Config.RGB_565;
			return BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length, options);
		} catch (OutOfMemoryError e) {
			Log.e(TAG, "OutOfMemoryError error = " + e.getMessage());
			return null;
		}
	}*/
	public static Bitmap makeBitmap(String file, int maxNumOfPixels) {
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			
			BitmapFactory.decodeFile(file, options);
			if (options.mCancel || options.outWidth == -1 || options.outHeight == -1) {
				return null;
			}
			options.inSampleSize = computeSampleSize(options, UNCONSTRAINED, maxNumOfPixels);
			options.inJustDecodeBounds = false;

			options.inDither = false;
			options.inPreferredConfig = Bitmap.Config.RGB_565;
			return BitmapFactory.decodeFile(file, options);
		} catch (OutOfMemoryError e) {
			Log.e(TAG, "OutOfMemoryError error = " + e.getMessage());
			return null;
		}
	}

	public static byte[] bitmapToByteArray(Bitmap bitmap) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.JPEG, 100, stream);
		byte[] byteArray = stream.toByteArray();
		return byteArray;
	}

	public static Bitmap byteArrayToBitmap(byte[] byteArray) {
		Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
		return bitmap;
	}

	public static Bitmap displayScreenWidthMatrix(Context context, Bitmap bitmap) {
		DisplayMetrics dm = context.getResources().getDisplayMetrics();

		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		float scale = ((float) dm.widthPixels) / width;

		Matrix matrix = new Matrix();
		matrix.postScale(scale, scale);

		Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
		return resizedBitmap;
	}
	
	/***************************************************************/
	// 이미지 Mime를 리턴하는 메서드
	/***************************************************************/

	public static String getMimeType(File file) {
	
		MimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
		String exString = MimeUtil.getMimeTypes(file).toString();
		
		if(exString.equals("image/jpeg"))
		{
			exString = ".jpg";	
		}else if(exString.equals("image/png"))
		{
			exString = ".png";
		}else if(exString.equals("image/gif"))
		{
			exString = ".gif";
		}

		return exString;
	}
	
	/***************************************************************/
	// 파일의 위치 (웹,디스크) 경로 리턴
	/***************************************************************/
	public static String getFilePath(String path) {
		if (path.startsWith("http")) {
			File file = ImageLoader.getInstance().getDiskCache().get(path);
			String url = file.getAbsolutePath()+ImageUtil.getMimeType(file);
			file.renameTo(new File(url));	
			return url;
		} else {
			return path;
		}	
	}
	public static String getFileAbsolutePath(String path) {
		if (path.startsWith("http")) {
			File file = ImageLoader.getInstance().getDiskCache().get(path);
			String url = file.getAbsolutePath();
			file.renameTo(new File(url));
			return url;
		} else {
			return path;
		}	
	}
	public static boolean isFilePath(String path) {
		File file = ImageLoader.getInstance().getDiskCache().get(path);			
		return file.isFile();
	}
}

/*class DocumentsContract {
    private static final String DOCUMENT_URIS = "com.android.providers.media.documents "
            + "com.android.externalstorage.documents "
            + "com.android.providers.downloads.documents "
            + "com.android.providers.media.documents";

    private static final String PATH_DOCUMENT = "document";
    private static final String TAG = "DocumentsContract";

    public static String getDocumentId(Uri documentUri) {
        final List<String> paths = documentUri.getPathSegments();
        if (paths.size() < 2) {
            throw new IllegalArgumentException("Not a document: " + documentUri);
        }
        if (!PATH_DOCUMENT.equals(paths.get(0))) {
            throw new IllegalArgumentException("Not a document: " + documentUri);
        }
        return paths.get(1);
    }

    public static boolean isDocumentUri(Uri uri) {
        final List<String> paths = uri.getPathSegments();
        if (paths.size() < 2) {
            return false;
        }
        if (!PATH_DOCUMENT.equals(paths.get(0))) {
            return false;
        }

        return DOCUMENT_URIS.contains(uri.getAuthority());
    }
}*/
