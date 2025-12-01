package wagemaker.uk.ui;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Property-based test for grid navigation wrapping.
 * 
 * **Feature: character-selection-menu, Property 1: Grid navigation wrapping**
 * **Validates: Requirements 3.2, 3.3, 3.4, 3.5, 3.6**
 */
public class GridNavigationWrappingPropertyTest {
    
    private static HeadlessApplication application;
    
    @BeforeAll
    public static void setUp() {
        // Mock GL20 for headless testing
        Gdx.gl = mock(GL20.class);
        Gdx.gl20 = mock(GL20.class);
        
        // Initialize headless application for LibGDX
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        application = new HeadlessApplication(new ApplicationAdapter() {}, config);
    }
    
    @AfterAll
    public static void tearDown() {
        if (application != null) {
            application.exit();
        }
    }
    
    /**
     * Property: For any grid position (column, row) and any navigation direction
     * (UP, DOWN, LEFT, RIGHT), navigating in that direction should move to the
     * correct adjacent cell with wrapping at boundaries.
     * 
     * This test runs 100 iterations as specified in the design document.
     */
    @Test
    public void gridNavigationWrapsCorrectly() {
        Random random = new Random();
        
        // Run 100 trials as specified in the design document
        for (int trial = 0; trial < 100; trial++) {
            // Generate random starting position
            int startColumn = random.nextInt(2);  // 0 or 1
            int startRow = random.nextInt(4);     // 0, 1, 2, or 3 (2x4 grid)
            
            // Generate random navigation direction
            Direction direction = Direction.values()[random.nextInt(Direction.values().length)];
            
            // Create dialog and set starting position
            CharacterSelectionDialog dialog = new CharacterSelectionDialog();
            
            // Navigate to starting position
            navigateToPosition(dialog, startColumn, startRow);
            
            // Verify we're at the starting position
            assertEquals(startColumn, dialog.getSelectedColumn(), 
                       "Should be at starting column (trial " + trial + ")");
            assertEquals(startRow, dialog.getSelectedRow(), 
                       "Should be at starting row (trial " + trial + ")");
            
            // Perform navigation
            switch (direction) {
                case UP:
                    dialog.navigateUp();
                    break;
                case DOWN:
                    dialog.navigateDown();
                    break;
                case LEFT:
                    dialog.navigateLeft();
                    break;
                case RIGHT:
                    dialog.navigateRight();
                    break;
            }
            
            // Calculate expected position with wrapping
            int expectedColumn = startColumn;
            int expectedRow = startRow;
            
            switch (direction) {
                case UP:
                    expectedRow = (startRow - 1 + 4) % 4;  // Wrap: 0->3, 1->0, 2->1, 3->2
                    break;
                case DOWN:
                    expectedRow = (startRow + 1) % 4;      // Wrap: 0->1, 1->2, 2->3, 3->0
                    break;
                case LEFT:
                    expectedColumn = (startColumn - 1 + 2) % 2;  // Wrap: 0->1, 1->0
                    break;
                case RIGHT:
                    expectedColumn = (startColumn + 1) % 2;      // Wrap: 0->1, 1->0
                    break;
            }
            
            // Verify navigation result
            assertEquals(expectedColumn, dialog.getSelectedColumn(),
                       "Column should be " + expectedColumn + " after navigating " + direction + 
                       " from (" + startColumn + "," + startRow + ") (trial " + trial + ")");
            assertEquals(expectedRow, dialog.getSelectedRow(),
                       "Row should be " + expectedRow + " after navigating " + direction + 
                       " from (" + startColumn + "," + startRow + ") (trial " + trial + ")");
            
            // Verify position is still within bounds
            assertTrue(dialog.getSelectedColumn() >= 0 && dialog.getSelectedColumn() <= 1,
                     "Column should be in range [0,1] (trial " + trial + ")");
            assertTrue(dialog.getSelectedRow() >= 0 && dialog.getSelectedRow() <= 3,
                     "Row should be in range [0,3] (trial " + trial + ")");
            
            // Clean up
            dialog.dispose();
        }
        
        System.out.println("Successfully verified grid navigation wrapping across 100 iterations");
    }
    
    /**
     * Helper method to navigate to a specific position in the grid.
     */
    private void navigateToPosition(CharacterSelectionDialog dialog, int targetColumn, int targetRow) {
        // Start from (0,0) and navigate to target position
        // Navigate right to reach target column
        for (int i = 0; i < targetColumn; i++) {
            dialog.navigateRight();
        }
        // Navigate down to reach target row
        for (int i = 0; i < targetRow; i++) {
            dialog.navigateDown();
        }
    }
    
    /**
     * Enum representing navigation directions.
     */
    private enum Direction {
        UP, DOWN, LEFT, RIGHT
    }
}
