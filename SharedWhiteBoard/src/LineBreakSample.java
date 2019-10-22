import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextLayout;
import java.text.AttributedCharacterIterator;
import java.awt.Color;

/**
 * This class demonstrates how to line-break and draw a paragraph 
 * of text using LineBreakMeasurer and TextLayout.
 *
 * This class constructs a LineBreakMeasurer from an
 * AttributedCharacterIterator.  It uses the LineBreakMeasurer
 * to create and draw TextLayouts (lines of text) which fit within 
 * the Component's width.
 */

public class LineBreakSample extends Component {

    // The LineBreakMeasurer used to line-break the paragraph.
    private LineBreakMeasurer lineMeasurer;

    // The index in the LineBreakMeasurer of the first character
    // in the paragraph.
    private int paragraphStart;

    // The index in the LineBreakMeasurer of the first character
    // after the end of the paragraph.
    private int paragraphEnd;

    public LineBreakSample(AttributedCharacterIterator paragraph) {

        FontRenderContext frc = SampleUtils.getDefaultFontRenderContext();

        paragraphStart = paragraph.getBeginIndex();
        paragraphEnd = paragraph.getEndIndex();

        // Create a new LineBreakMeasurer from the paragraph.
        lineMeasurer = new LineBreakMeasurer(paragraph, frc);
    }

    public void paint(Graphics g) {

        Graphics2D graphics2D = (Graphics2D) g;

        // Set formatting width to width of Component.
        Dimension size = getSize();
        float formatWidth = (float) size.width;

        float drawPosY = 0;

        lineMeasurer.setPosition(paragraphStart);

        // Get lines from lineMeasurer until the entire
        // paragraph has been displayed.
        while (lineMeasurer.getPosition() < paragraphEnd) {

            // Retrieve next layout.
            TextLayout layout = lineMeasurer.nextLayout(formatWidth);
            // Move y-coordinate by the ascent of the layout.
            drawPosY += layout.getAscent();

            // Compute pen x position.  If the paragraph is 
            // right-to-left, we want to align the TextLayouts
            // to the right edge of the panel.
            float drawPosX;
            if (layout.isLeftToRight()) {
                drawPosX = 0;
            }
            else {
                drawPosX = formatWidth - layout.getAdvance();
            }

            // Draw the TextLayout at (drawPosX, drawPosY).
            layout.draw(graphics2D, drawPosX, drawPosY);

            // Move y-coordinate in preparation for next layout.
            drawPosY += layout.getDescent() + layout.getLeading();
        }
    }

    public static void main(String[] args) {

        // If no command-line arguments, use these:
        if (args.length == 0) {
            args = new String[] { "-text", "longenglish" };
        }

        AttributedCharacterIterator text = SampleUtils.getText(args);

        Component sample = new LineBreakSample(text);
        SampleUtils.showComponentInFrame(sample, "Line Break Sample");
    }
}