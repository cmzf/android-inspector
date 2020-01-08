package com.github.cmzf.androidinspector;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.accessibility.AccessibilityNodeInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UiObject {

    private static final String TAG = UiObject.class.getCanonicalName();

    private final AccessibilityNodeInfo mInfo;

    private UiObject(AccessibilityNodeInfo info) {
        mInfo = info;
    }

    public static UiObject wrap(AccessibilityNodeInfo info) {
        if (info == null) {
            return null;
        }
        return new UiObject(info);
    }

    public UiObject parent() {
        AccessibilityNodeInfo info = mInfo.getParent();
        return info != null ? wrap(info) : null;
    }

    @Override
    public String toString() {
        return "<" + getCls()
                + " hash='" + mInfo.hashCode() + "'"
                + (!"".equals(getId()) ? " id='" + getId() + "'" : "")
                + (!"".equals(getPkg()) ? " pkg='" + getPkg() + "'" : "")
                + (!"".equals(getText()) ? " text='" + getText() + "'" : "")
                + (!"".equals(getDesc()) ? " desc='" + getDesc() + "'" : "")
                + " />";
    }

    public UiTree<UiObject> uiTree() {
        return uiTree(this);
    }

    public UiTree<UiObject> uiTree(UiObject node) {
        ArrayList<UiTree<UiObject>> treeChildren = new ArrayList<>();
        ArrayList<UiObject> children = node.children();
        for (UiObject child : children) {
            treeChildren.add(treeChildren.size(), uiTree(child));
        }
        return new UiTree<>(node, treeChildren);
    }

    public ArrayList<UiObject> children() {
        if (mInfo == null) {
            return new ArrayList<>();
        }
        int childCount = mInfo.getChildCount();
        ArrayList<UiObject> list = new ArrayList<>(mInfo.getChildCount());
        for (int i = 0; i < childCount; i++) {
            AccessibilityNodeInfo info = mInfo.getChild(i);
            if (info != null) {
                list.add(wrap(info));
            }
        }
        return list;
    }

    public ArrayList<UiObject> descendants() {
        return descendants(false);
    }

    public ArrayList<UiObject> descendants(boolean withSelf) {
        ArrayList<UiObject> result = new ArrayList<>();
        if (withSelf) {
            result.add(this);
        }

        ArrayList<UiObject> items = children();
        for (UiObject item : items) {
            result.addAll(item.descendants(true));
        }
        return result;
    }

    public String getId() {
        return Objects.toString(mInfo.getViewIdResourceName(), "");
    }

    public String getHash() {
        return String.valueOf(mInfo.hashCode());
    }

    public String getText() {
        if (mInfo.isPassword()) {
            return "";
        }
        return Objects.toString(mInfo.getText(), "");
    }

    public String getDesc() {
        return Objects.toString(mInfo.getContentDescription(), "");
    }

    public String getCls() {
        return Objects.toString(mInfo.getClassName(), "");
    }

    public String getPkg() {
        return Objects.toString(mInfo.getPackageName(), "");
    }

    public Rect getBounds() {
        Rect bounds = new Rect();
        mInfo.getBoundsInScreen(bounds);
        return bounds;
    }

    public List<String> getExtraData() {
        return mInfo.getAvailableExtraData();
    }

    public int getChildCount() {
        return mInfo.getChildCount();
    }

    public int getDrawingOrder() {
        return mInfo.getDrawingOrder();
    }

    public Bundle getExtras() {
        return mInfo.getExtras();
    }

    public String getHintText() {
        return Objects.toString(mInfo.getHintText(), "");
    }

    public int getInputType() {
        return mInfo.getInputType();
    }

    public int getMaxTextLength() {
        return mInfo.getMaxTextLength();
    }

    public int getMovementGranularities() {
        return mInfo.getMovementGranularities();
    }

    public int getTextSelectionEnd() {
        return mInfo.getTextSelectionEnd();
    }

    public int getTextSelectionStart() {
        return mInfo.getTextSelectionStart();
    }

    public int getWindowId() {
        return mInfo.getWindowId();
    }

    public boolean isCheckable() {
        return mInfo.isCheckable();
    }

    public boolean isChecked() {
        return mInfo.isChecked();
    }

    public boolean isClickable() {
        return mInfo.isClickable();
    }

    public boolean isEditable() {
        return mInfo.isEditable();
    }

    public boolean isEnabled() {
        return mInfo.isEnabled();
    }

    public boolean isLongClickable() {
        return mInfo.isLongClickable();
    }

    public boolean isMultiLine() {
        return mInfo.isMultiLine();
    }

    public boolean isPassword() {
        return mInfo.isPassword();
    }

    public boolean isScrollable() {
        return mInfo.isScrollable();
    }

    public boolean isSelected() {
        return mInfo.isSelected();
    }

    public boolean isVisibleToUser() {
        return mInfo.isVisibleToUser();
    }

    public boolean isAccessibilityFocused() {
        return mInfo.isAccessibilityFocused();
    }

    public boolean isContentInvalid() {
        return mInfo.isContentInvalid();
    }

    public boolean isContextClickable() {
        return mInfo.isContextClickable();
    }

    public boolean isDismissable() {
        return mInfo.isDismissable();
    }

    public boolean isFocusable() {
        return mInfo.isFocusable();
    }

    public boolean isFocused() {
        return mInfo.isFocused();
    }

    public boolean isImportantForAccessibility() {
        return mInfo.isImportantForAccessibility();
    }

    public boolean isShowingHintText() {
        return mInfo.isShowingHintText();
    }

    public static class UiTree<T> implements Serializable {
        private T node;
        private ArrayList<UiTree<T>> children;

        public UiTree(T node, ArrayList<UiTree<T>> children) {
            this.node = node;
            if (children.size() > 0) {
                this.children = children;
            }
        }

        public T getNode() {
            return node;
        }

        public ArrayList<UiTree<T>> getChildren() {
            return children;
        }
    }
}
