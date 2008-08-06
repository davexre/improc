package com.test.image;


public class ImgTest {

	public static final String finName = "C:/Users/S/ImageProcess/images/testimg.tif";
//	public static final String finName = "c:/users/monitor-test/5248-001.tif";

	
//	public static void main(String[] args) throws IOException {
//		Iterator it = ImageIO.getImageReadersByFormatName("tif");
//		if (it.hasNext()) {
//			ImageReader ir = (ImageReader) it.next();
//			ImageInputStream iis = ImageIO.createImageInputStream(new File(finName));
//			ir.setInput(iis, true);
//			TIFFImageMetadata iiom = (TIFFImageMetadata) ir.getImageMetadata(0);
//			for (int i = 0; i < 65536; i++) {
//				try {
//					TIFFField fld = iiom.getTIFFField(i);
//					if (fld == null)
//						continue;
////					if (fld.getType() == 2) // ASCII
////						System.out.println(fld.getTag().getName() + "\t" + fld.getAsString(0));
//					System.out.println(fld.getTag().getName() + "\t" + fld.getType() + "\t" + fld.getTypeName(fld.getType()));
//					
//				} catch (Exception e) {
//					
//				}		
//			}
//			System.out.println(iiom.getNativeMetadataFormatName());
//			
//			TIFFIFD ifd = iiom.getRootIFD();
//			TIFFField flds[] = ifd.getTIFFFields();
//			for (TIFFField fld : flds)
//				if (fld != null) {
//					//if (fld.getType() == 2)
//						System.out.println(fld.getTag().getName() + "\t" + fld.getType() + "\t" + fld.getTypeName(fld.getType()));	
//				}
//					
//			
//			System.out.println("resolution= " + iiom.getTIFFField(<your tag number. For example 256 or 296>).getAsInt(0));
//		}		
//	}
//	
//	
// //*** using jai
//	public static void main(String[] args) {
//		RenderedOp im0 = JAI.create("fileload", finName);
//		String[] pn = im0.getPropertyNames();
//		for (String str : pn)
//			System.out.println(str);
//		TIFFDirectory td = (TIFFDirectory)im0.getProperty("tiff_directory");
//		TIFFField fields[] = td.getFields();
//		System.out.println("Number of fields " + fields.length);
//		for (TIFFField fld : fields) {
//			if ((fld != null))
//			if ((fld.getType()==TIFFField.TIFF_ASCII)) {
//				for (int i = 0; i < fld.getCount(); i++)
//					System.out.println(fld.getTag() + "\t" + fld.getAsString(i));
//			} else
//				System.out.println(fld.getType() + "\t" + fld.getTag());
//			
//		}
//	}

// *** using jiu	
//	public static void main(String[] args) throws IOException, MissingParameterException, OperationFailedException {
//		TIFFCodec codec = new TIFFCodec();
//		codec.setFile(finName, CodecMode.LOAD);
//		codec.process();
//		System.out.println(codec.getTagName(TIFFConstants.TAG_COPYRIGHT));
//	}

}
