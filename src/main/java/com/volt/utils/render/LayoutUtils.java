package com.volt.utils.render;

import lombok.experimental.UtilityClass;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
public final class LayoutUtils {

    public static void drawLayoutBounds(DrawContext context, FlexContainer container) {
        for (FlexItem item : container.items) {
            GuiUtils.drawBorder(context, item.getX(), item.getY(), item.getWidth(), item.getHeight(),
                    0, 1, new Color(255, 0, 0, 100));
        }
    }

    public static void drawLayoutBounds(DrawContext context, GridContainer container) {
        for (GridItem item : container.items) {
            GuiUtils.drawBorder(context, item.getX(), item.getY(), item.getWidth(), item.getHeight(),
                    0, 1, new Color(0, 255, 0, 100));
        }
    }

    public static class FlexContainer {
        private final List<FlexItem> items = new ArrayList<>();
        private FlexDirection direction = FlexDirection.ROW;
        private JustifyContent justifyContent = JustifyContent.START;
        private AlignItems alignItems = AlignItems.START;
        private int gap = 0;
        private Padding padding = new Padding(0);

        public FlexContainer setDirection(FlexDirection direction) {
            this.direction = direction;
            return this;
        }

        public FlexContainer setJustifyContent(JustifyContent justifyContent) {
            this.justifyContent = justifyContent;
            return this;
        }

        public FlexContainer setAlignItems(AlignItems alignItems) {
            this.alignItems = alignItems;
            return this;
        }

        public FlexContainer setGap(int gap) {
            this.gap = gap;
            return this;
        }

        public FlexContainer setPadding(int padding) {
            this.padding = new Padding(padding);
            return this;
        }

        public FlexContainer setPadding(int top, int right, int bottom, int left) {
            this.padding = new Padding(top, right, bottom, left);
            return this;
        }

        public FlexContainer addItem(FlexItem item) {
            items.add(item);
            return this;
        }

        public void layout(int containerX, int containerY, int containerWidth, int containerHeight) {
            if (items.isEmpty()) return;

            int contentX = containerX + padding.left;
            int contentY = containerY + padding.top;
            int contentWidth = containerWidth - padding.left - padding.right;
            int contentHeight = containerHeight - padding.top - padding.bottom;

            boolean isRow = direction == FlexDirection.ROW || direction == FlexDirection.ROW_REVERSE;
            int mainAxisSize = isRow ? contentWidth : contentHeight;
            int crossAxisSize = isRow ? contentHeight : contentWidth;

            int totalItemSize = 0;
            int flexGrowSum = 0;

            for (FlexItem item : items) {
                if (isRow) {
                    totalItemSize += item.getPreferredWidth();
                } else {
                    totalItemSize += item.getPreferredHeight();
                }
                flexGrowSum += item.flexGrow;
            }

            totalItemSize += gap * (items.size() - 1);

            int availableSpace = Math.max(0, mainAxisSize - totalItemSize);

            int[] positions = calculateMainAxisPositions(mainAxisSize, totalItemSize, availableSpace);

            for (int i = 0; i < items.size(); i++) {
                FlexItem item = items.get(i);
                int itemMainPos = positions[i];
                int itemCrossPos = calculateCrossAxisPosition(item, crossAxisSize);

                int itemX, itemY, itemWidth, itemHeight;

                if (isRow) {
                    itemX = contentX + itemMainPos;
                    itemY = contentY + itemCrossPos;
                    itemWidth = item.getPreferredWidth();
                    if (item.flexGrow > 0 && availableSpace > 0) {
                        itemWidth += (availableSpace * item.flexGrow) / flexGrowSum;
                    }
                    itemHeight = alignItems == AlignItems.STRETCH ? crossAxisSize : item.getPreferredHeight();
                } else {
                    itemX = contentX + itemCrossPos;
                    itemY = contentY + itemMainPos;
                    itemWidth = alignItems == AlignItems.STRETCH ? crossAxisSize : item.getPreferredWidth();
                    itemHeight = item.getPreferredHeight();
                    if (item.flexGrow > 0 && availableSpace > 0) {
                        itemHeight += (availableSpace * item.flexGrow) / flexGrowSum;
                    }
                }

                item.setBounds(itemX, itemY, itemWidth, itemHeight);
            }
        }

        private int[] calculateMainAxisPositions(int containerSize, int totalItemSize, int availableSpace) {
            int[] positions = new int[items.size()];

            switch (justifyContent) {
                case START:
                    int pos = 0;
                    for (int i = 0; i < items.size(); i++) {
                        positions[i] = pos;
                        pos += (direction == FlexDirection.ROW ? items.get(i).getPreferredWidth() : items.get(i).getPreferredHeight()) + gap;
                    }
                    break;

                case END:
                    pos = containerSize - totalItemSize;
                    for (int i = 0; i < items.size(); i++) {
                        positions[i] = pos;
                        pos += (direction == FlexDirection.ROW ? items.get(i).getPreferredWidth() : items.get(i).getPreferredHeight()) + gap;
                    }
                    break;

                case CENTER:
                    pos = (containerSize - totalItemSize) / 2;
                    for (int i = 0; i < items.size(); i++) {
                        positions[i] = pos;
                        pos += (direction == FlexDirection.ROW ? items.get(i).getPreferredWidth() : items.get(i).getPreferredHeight()) + gap;
                    }
                    break;

                case SPACE_BETWEEN:
                    if (items.size() == 1) {
                        positions[0] = 0;
                    } else {
                        int spaceBetween = availableSpace / (items.size() - 1);
                        pos = 0;
                        for (int i = 0; i < items.size(); i++) {
                            positions[i] = pos;
                            pos += (direction == FlexDirection.ROW ? items.get(i).getPreferredWidth() : items.get(i).getPreferredHeight()) + gap + spaceBetween;
                        }
                    }
                    break;

                case SPACE_AROUND:
                    int spaceAround = availableSpace / items.size();
                    pos = spaceAround / 2;
                    for (int i = 0; i < items.size(); i++) {
                        positions[i] = pos;
                        pos += (direction == FlexDirection.ROW ? items.get(i).getPreferredWidth() : items.get(i).getPreferredHeight()) + gap + spaceAround;
                    }
                    break;

                case SPACE_EVENLY:
                    int spaceEvenly = availableSpace / (items.size() + 1);
                    pos = spaceEvenly;
                    for (int i = 0; i < items.size(); i++) {
                        positions[i] = pos;
                        pos += (direction == FlexDirection.ROW ? items.get(i).getPreferredWidth() : items.get(i).getPreferredHeight()) + gap + spaceEvenly;
                    }
                    break;
            }

            return positions;
        }

        private int calculateCrossAxisPosition(FlexItem item, int crossAxisSize) {
            boolean isRow = direction == FlexDirection.ROW || direction == FlexDirection.ROW_REVERSE;
            int itemCrossSize = isRow ? item.getPreferredHeight() : item.getPreferredWidth();

            switch (alignItems) {
                case START:
                    return 0;
                case END:
                    return crossAxisSize - itemCrossSize;
                case CENTER:
                    return (crossAxisSize - itemCrossSize) / 2;
                case STRETCH:
                    return 0;
                default:
                    return 0;
            }
        }

        public enum FlexDirection {
            ROW, COLUMN, ROW_REVERSE, COLUMN_REVERSE
        }

        public enum JustifyContent {
            START, END, CENTER, SPACE_BETWEEN, SPACE_AROUND, SPACE_EVENLY
        }

        public enum AlignItems {
            START, END, CENTER, STRETCH
        }
    }

    public static class FlexItem {
        private final int preferredWidth;
        private final int preferredHeight;
        private final Runnable renderer;
        private int x, y, width, height;
        private int flexGrow = 0;
        private int flexShrink = 1;

        public FlexItem(int preferredWidth, int preferredHeight, Runnable renderer) {
            this.preferredWidth = preferredWidth;
            this.preferredHeight = preferredHeight;
            this.renderer = renderer;
        }

        public FlexItem setFlexGrow(int flexGrow) {
            this.flexGrow = flexGrow;
            return this;
        }

        public FlexItem setFlexShrink(int flexShrink) {
            this.flexShrink = flexShrink;
            return this;
        }

        public void setBounds(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public void render() {
            if (renderer != null) {
                renderer.run();
            }
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public int getPreferredWidth() {
            return preferredWidth;
        }

        public int getPreferredHeight() {
            return preferredHeight;
        }
    }

    public static class GridContainer {
        private final List<GridItem> items = new ArrayList<>();
        private final int columns;
        private final int rows;
        private int columnGap = 0;
        private int rowGap = 0;
        private Padding padding = new Padding(0);

        public GridContainer(int columns, int rows) {
            this.columns = columns;
            this.rows = rows;
        }

        public GridContainer setGap(int gap) {
            this.columnGap = gap;
            this.rowGap = gap;
            return this;
        }

        public GridContainer setGap(int columnGap, int rowGap) {
            this.columnGap = columnGap;
            this.rowGap = rowGap;
            return this;
        }

        public GridContainer setPadding(int padding) {
            this.padding = new Padding(padding);
            return this;
        }

        public GridContainer addItem(GridItem item) {
            items.add(item);
            return this;
        }

        public void layout(int containerX, int containerY, int containerWidth, int containerHeight) {
            int contentX = containerX + padding.left;
            int contentY = containerY + padding.top;
            int contentWidth = containerWidth - padding.left - padding.right;
            int contentHeight = containerHeight - padding.top - padding.bottom;

            int cellWidth = (contentWidth - columnGap * (columns - 1)) / columns;
            int cellHeight = (contentHeight - rowGap * (rows - 1)) / rows;

            for (GridItem item : items) {
                if (item.column >= columns || item.row >= rows) continue;

                int itemX = contentX + item.column * (cellWidth + columnGap);
                int itemY = contentY + item.row * (cellHeight + rowGap);
                int itemWidth = cellWidth + (cellWidth + columnGap) * (item.columnSpan - 1);
                int itemHeight = cellHeight + (cellHeight + rowGap) * (item.rowSpan - 1);

                item.setBounds(itemX, itemY, itemWidth, itemHeight);
            }
        }

        public void render() {
            for (GridItem item : items) {
                item.render();
            }
        }
    }

    public static class GridItem {
        private final int column;
        private final int row;
        private final Runnable renderer;
        private int x, y, width, height;
        private int columnSpan = 1, rowSpan = 1;

        public GridItem(int column, int row, Runnable renderer) {
            this.column = column;
            this.row = row;
            this.renderer = renderer;
        }

        public GridItem setSpan(int columnSpan, int rowSpan) {
            this.columnSpan = columnSpan;
            this.rowSpan = rowSpan;
            return this;
        }

        public void setBounds(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public void render() {
            if (renderer != null) {
                renderer.run();
            }
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }

    public static class Anchor {
        public static final Anchor TOP_LEFT = new Anchor(Horizontal.LEFT, Vertical.TOP);
        public static final Anchor TOP_CENTER = new Anchor(Horizontal.CENTER, Vertical.TOP);
        public static final Anchor TOP_RIGHT = new Anchor(Horizontal.RIGHT, Vertical.TOP);
        public static final Anchor CENTER_LEFT = new Anchor(Horizontal.LEFT, Vertical.CENTER);
        public static final Anchor CENTER = new Anchor(Horizontal.CENTER, Vertical.CENTER);
        public static final Anchor CENTER_RIGHT = new Anchor(Horizontal.RIGHT, Vertical.CENTER);
        public static final Anchor BOTTOM_LEFT = new Anchor(Horizontal.LEFT, Vertical.BOTTOM);
        public static final Anchor BOTTOM_CENTER = new Anchor(Horizontal.CENTER, Vertical.BOTTOM);
        public static final Anchor BOTTOM_RIGHT = new Anchor(Horizontal.RIGHT, Vertical.BOTTOM);
        private final Horizontal horizontal;
        private final Vertical vertical;
        private final int offsetX;
        private final int offsetY;

        public Anchor(Horizontal horizontal, Vertical vertical) {
            this(horizontal, vertical, 0, 0);
        }

        public Anchor(Horizontal horizontal, Vertical vertical, int offsetX, int offsetY) {
            this.horizontal = horizontal;
            this.vertical = vertical;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }

        public Position calculate(int screenWidth, int screenHeight, int elementWidth, int elementHeight) {
            int x, y;

            switch (horizontal) {
                case LEFT:
                    x = offsetX;
                    break;
                case CENTER:
                    x = (screenWidth - elementWidth) / 2 + offsetX;
                    break;
                case RIGHT:
                    x = screenWidth - elementWidth - offsetX;
                    break;
                default:
                    x = offsetX;
            }

            switch (vertical) {
                case TOP:
                    y = offsetY;
                    break;
                case CENTER:
                    y = (screenHeight - elementHeight) / 2 + offsetY;
                    break;
                case BOTTOM:
                    y = screenHeight - elementHeight - offsetY;
                    break;
                default:
                    y = offsetY;
            }

            return new Position(x, y);
        }

        public enum Horizontal {LEFT, CENTER, RIGHT}

        public enum Vertical {TOP, CENTER, BOTTOM}
    }

    public record Position(int x, int y) {
    }

    public record Size(int width, int height) {
    }

    public record Padding(int top, int right, int bottom, int left) {
        public Padding(int all) {
            this(all, all, all, all);
        }

        public Padding(int vertical, int horizontal) {
            this(vertical, horizontal, vertical, horizontal);
        }

    }

    public record Margin(int top, int right, int bottom, int left) {
        public Margin(int all) {
            this(all, all, all, all);
        }

        public Margin(int vertical, int horizontal) {
            this(vertical, horizontal, vertical, horizontal);
        }

    }

    public static class Responsive {
        public static int scaleWithScreen(int baseValue, int baseScreenWidth, int currentScreenWidth) {
            return (baseValue * currentScreenWidth) / baseScreenWidth;
        }

        public static int scaleWithDPI(int baseValue, float scaleFactor) {
            return Math.round(baseValue * scaleFactor);
        }

        public static Size calculateResponsiveSize(Size baseSize, int screenWidth, int screenHeight) {
            float scaleX = (float) screenWidth / 1920;
            float scaleY = (float) screenHeight / 1080;
            float scale = Math.min(scaleX, scaleY);

            return new Size(
                    Math.round(baseSize.width * scale),
                    Math.round(baseSize.height * scale)
            );
        }

        public static int getFontSizeForScreen(int baseFontSize, int screenWidth) {
            if (screenWidth <= 1366) return baseFontSize - 2;
            if (screenWidth <= 1920) return baseFontSize;
            if (screenWidth <= 2560) return baseFontSize + 2;
            return baseFontSize + 4;
        }
    }
} 