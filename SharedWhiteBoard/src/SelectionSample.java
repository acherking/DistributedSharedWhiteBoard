import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextHitInfo;
import java.awt.font.TextLayout;
import java.awt.geom.Point2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseEvent;
import java.text.AttributedCharacterIterator;

/**
 * This class demonstrates how to select text with TextLayout.
 *
 * When the mouse is dragged inside this panel, the selection 
 * range is updated to reflect the mouse position.  TextLayout's
 * hitTestChar() method is used to get the selection endpoint
 * corresponding to the mouse position.  getLogicalHighlightShape()
 * returns the Shape which is filled with the highlight color to
 * show the selection range.
 */
public class SelectionSample extends Component {

    // Colors to use for the strong and weak carets.
    private static final Color STRONG_CARET_COLOR = Color.red;
    private static final Color WEAK_CARET_COLOR = Color.black;
    // Color to use for highlighting.
    private static final Color HIGHLIGHT_COLOR = Color.pink;
    // Color to use for text.
    private static final Color TEXT_COLOR = Color.black;
    // The TextLayout to hit-test and select.
    private TextLayout textLayout;
    // The insertion index of the initial mouse click;  one
    // end of the selection range.  During a mouse drag this end
    // of the selection is constant.
    private int anchorEnd;

    // The insertion index of the current mouse location;  the
    // other end of the selection range.  This changes during a mouse
    // drag.
    private int activeEnd;
    public SelectionSample(AttributedCharacterIterator text) {
        FontRenderContext frc = SampleUtils.getDefaultFontRenderContext();
        // Create a new TextLayout from the given text.
        textLayout = new TextLayout(text, frc);
        // Initilize activeEnd and anchorEnd.
        anchorEnd = 0;
        activeEnd = 0;

        addMouseListener(new SelectionMouseListener());
        addMouseMotionListener(new SelectionMouseMotionListener());
    }

    /**
     * Compute a location within this Panel for textLayout's origin,
     * such that textLayout is centered horizontally and vertically.
     *
     * Note that this location is unknown to textLayout;  it is used only
     * by this Panel for positioning.
     */
    private Point2D computeLayoutOrigin() {
        Dimension size = getSize();
        Point2D.Float origin = new Point2D.Float();
        origin.x = (float) (size.width - textLayout.getAdvance()) / 2;
        origin.y = (float) (size.height - textLayout.getDescent() + textLayout.getAscent()) / 2;
        return origin;
    }

    /**
     * Draw textLayout and either: the selection range (if a range of characters
     * is selected), or the carets (if the selection range is 0-length).
     */
    public void paint(Graphics g) {
        Graphics2D graphics2D = (Graphics2D) g;
        Point2D origin = computeLayoutOrigin();
        // Since the selection and caret Shapes are relative to the 
        // origin of textLayout, we'll translate the graphics so that 
        // the origin of the graphics is where we want textLayout to
        // appear.
        graphics2D.translate(origin.getX(), origin.getY());
        // If the insertion indices of the two selection endpoints
        // are equal, we will draw caret(s) at the insertion index.
        // Otherwise we will draw a highlight region between the 
        // insertion indices.

        boolean haveCaret = anchorEnd == activeEnd;

        if (!haveCaret) {
            // Retrieve highlight region for selection range.
            Shape highlight = textLayout.getLogicalHighlightShape(anchorEnd, activeEnd);
            // Fill the highlight region with the highlight color.
            graphics2D.setColor(HIGHLIGHT_COLOR);
            graphics2D.fill(highlight);
        }

        // Draw textLayout.
        graphics2D.setColor(TEXT_COLOR);
        textLayout.draw(graphics2D, 0, 0);

        if (haveCaret) {
            // Retrieve caret Shapes for the insertion index.
            Shape[] carets = textLayout.getCaretShapes(anchorEnd);

            // Draw the carets.  carets[0] is the strong caret, and
            // is never null.  carets[1], if it is not null, is the
            // weak caret.
            graphics2D.setColor(STRONG_CARET_COLOR);
            graphics2D.draw(carets[0]);

            if (carets[1] != null) {
                graphics2D.setColor(WEAK_CARET_COLOR);
                graphics2D.draw(carets[1]);
            }
        }
    }

    private class SelectionMouseMotionListener extends MouseMotionAdapter {
        /**
         * Set the active selection endpoint to
         * the character position of the mouse.
         */
        public void mouseDragged(MouseEvent e) {
            Point2D origin = computeLayoutOrigin();

            // Compute the mouse location relative to
            // textLayout's origin.
            float clickX = (float) (e.getX() - origin.getX());
            float clickY = (float) (e.getY() - origin.getY());

            // Get the character position of the mouse location.
            TextHitInfo position = textLayout.hitTestChar(clickX, clickY);
            int newActiveEnd = position.getInsertionIndex();

            // If newActiveEnd is different from activeEnd, update activeEnd
            // and repaint the Panel so the new selection will be displayed.
            if (activeEnd != newActiveEnd) {
                activeEnd = newActiveEnd;
                repaint();
            }
        }
    }

    private class SelectionMouseListener extends MouseAdapter {
        /**
         * Set the active and anchor selection endpoints to
         * the character position of the mouse click.
         */
        public void mousePressed(MouseEvent e) {
            Point2D origin = computeLayoutOrigin();

            // Compute the mouse location relative to
            // textLayout's origin.
            float clickX = (float) (e.getX() - origin.getX());
            float clickY = (float) (e.getY() - origin.getY());

            // Set the anchor and active ends of the selection 
            // to the character position of the mouse location.
            TextHitInfo position = textLayout.hitTestChar(clickX, clickY);
            anchorEnd = position.getInsertionIndex();
            activeEnd = anchorEnd;

            // Repaint the Panel so the new selection will be displayed.
            repaint();
        }
    }

    public static void main(String[] args) {

        AttributedCharacterIterator text = SampleUtils.getText(args);

        Component sample = new SelectionSample(text);
        SampleUtils.showComponentInFrame(sample, "Selection Sample");
    }
}