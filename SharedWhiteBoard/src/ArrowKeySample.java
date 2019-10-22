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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.AttributedCharacterIterator;

/**
 * This class demonstrates how to use TextLayout to respond
 * to left and right arrow keys.
 *
 * This class maintains an insertion offset, which is initially 0.
 * It displays a TextLayout and the caret(s) for the insertion offset.
 * The insertion offset can be moved by pressing the left and right
 * arrow keys.  TextLayout's getNextLeftHit() and getNextRightHit()
 * methods are used to get the new insertion offset.
 */

public class ArrowKeySample extends Component {

    // Colors to use for the strong and weak carets.
    private static final Color STRONG_CARET_COLOR = Color.red;
    private static final Color WEAK_CARET_COLOR = Color.black;

    // The TextLayout to draw and caret through.
    private TextLayout textLayout;

    // The current insertion index.
    private int insertionIndex = 0;
    public ArrowKeySample(AttributedCharacterIterator text) {
        FontRenderContext frc = SampleUtils.getDefaultFontRenderContext();
        // Create a new TextLayout from the given text.
        textLayout = new TextLayout(text, frc);
        addKeyListener(new ArrowKeyListener());
    }

    /**
     * Compute a location within this Component for textLayout's origin,
     * such that textLayout is centered horizontally and vertically.
     *
     * Note that this location is unknown to textLayout;  it is used only
     * by this Component for positioning.
     */

    private Point2D computeLayoutOrigin() {
        Dimension size = getSize();
        Point2D.Float origin = new Point2D.Float();
        origin.x = (float) (size.width - textLayout.getAdvance()) / 2;
        origin.y = (float) (size.height - textLayout.getDescent() + textLayout.getAscent()) / 2;
        return origin;
    }

    /**
     * Draw textLayout and the carets corresponding to the current
     * insertion index.
     */

    public void paint(Graphics g) {
        Graphics2D graphics2D = (Graphics2D) g;
        Point2D origin = computeLayoutOrigin();

        // Since the caret Shapes are relative to the origin of
        // textLayout, we'll translate the graphics so that the
        // origin of the graphics is where we want textLayout to
        // appear.
        graphics2D.translate(origin.getX(), origin.getY());

        // Draw textLayout.
        textLayout.draw(graphics2D, 0, 0);

        // Retrieve caret Shapes for the current insertion index.
        Shape[] carets = textLayout.getCaretShapes(insertionIndex);

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

    private class ArrowKeyListener extends KeyAdapter {
        /**
         * Update the insertion index in response to an arrow key.
         */
        private void handleArrowKey(boolean rightArrow) {
            TextHitInfo newPosition;
            if (rightArrow) {
                newPosition = textLayout.getNextRightHit(insertionIndex);
            }
            else {
                newPosition = textLayout.getNextLeftHit(insertionIndex);
            }

            // getNextRightHit() / getNextLeftHit() will return null if 
            // there is not a caret position to the right (left) of the 
            // current position.
            if (newPosition != null) {
                // Update insertionIndex.
                insertionIndex = newPosition.getInsertionIndex();
                // Repaint the Component so the new caret(s) will be displayed.
                repaint();
            }
        }
        public void keyPressed(KeyEvent e) {
            int keyCode = e.getKeyCode();
            if (keyCode == KeyEvent.VK_LEFT ||
                    keyCode == KeyEvent.VK_RIGHT) {
                handleArrowKey(keyCode == KeyEvent.VK_RIGHT);
            }
        }
    }

    public static void main(String[] args) {
        AttributedCharacterIterator text = SampleUtils.getText(args);
        Component sample = new ArrowKeySample(text);
        SampleUtils.showComponentInFrame(sample, "Arrow Key Sample");
    }
}