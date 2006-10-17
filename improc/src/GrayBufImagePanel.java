import java.awt.Container;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import javax.swing.JApplet;
import javax.swing.JPanel;

/** Display the DrawingPanel with BufferedImage on the applet.**/
class CreateBufferedGrayImageApplet extends JApplet
{
  private static final long serialVersionUID = 1L;
  GrayBufImagePanel fBufImagePanel = null;
  
  public void init ()   {
    Container content_pane = getContentPane ();

    // Create an instance of BufImagePanel
    fBufImagePanel = new GrayBufImagePanel ();

    // Add the BufImagePanel to the contentPane.
    content_pane.add (fBufImagePanel);
  } // init

  // Invoke the init method in BufImagePanel to build the image.
  public void start () {
    fBufImagePanel.makeImage ();
  }
} // class CreateBufferedGrayImageApplet

/** Create an image from a pixel array using BufferedImage. **/
class GrayBufImagePanel extends JPanel
{
  private static final long serialVersionUID = 1L;
  BufferedImage fBufferedImage = null;
  int fWidth = 0, fHeight = 0;
  byte [] fPixels = null;

  /** Build a BufferedImage from a pixel array. **/
  void makeImage () {

    fWidth  = getSize ().width;
    fHeight = getSize ().height;
    fPixels = new byte [fWidth * fHeight];

    // Create an array of pixels with varying aRGB components
    int i=0;
    int half_width = fWidth/2;
    for  (int y = 0; y < fHeight; y++){
      for ( int x = 0; x < fWidth; x++){

        // Peak white in middle
        int gray = (255 * x)/half_width;
        if (x > half_width) gray = 510 - gray;
        fPixels[i++] = (byte) gray;
      }
    }
    // Create a BufferedIamge of the gray values in bytes.
    fBufferedImage =
       new BufferedImage (fWidth, fHeight, BufferedImage.TYPE_BYTE_GRAY);

    // Get the writable raster so that data can be changed.
    WritableRaster wr = fBufferedImage.getRaster();

    // Now write the byte data to the raster
    wr.setDataElements (0, 0, fWidth, fHeight, fPixels);
  }

  /** Draw the image on the panel. **/
  public void paintComponent (Graphics g) {
    super.paintComponent (g);
    if (fBufferedImage != null)
        g.drawImage (fBufferedImage, 0, 0, this );
  } // makeImage

} // class GrayBufImagePanel
