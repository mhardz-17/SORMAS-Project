package de.symeda.sormas.app.component.controls;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.databinding.BindingAdapter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import de.symeda.sormas.api.I18nProperties;
import de.symeda.sormas.app.R;
import de.symeda.sormas.app.util.ControlLabelOnTouchListener;

public abstract class ControlPropertyField<T> extends LinearLayout {

    // Views

    private View labelFrame;
    protected TextView label;

    // Attributes

    private String description;
    private String caption;
    private boolean showCaption;
    private int textAlignment;
    private int gravity;
    private int imeOptions;
    private boolean slim;
    private Boolean captionCapitalized;
    private Boolean captionItalic;
    private ControlPropertyField dependencyParentField;
    private Object dependencyParentValue;
    private boolean dependencyParentVisibility = true;

    // Other fields

    private ArrayList<ValueChangeListener> valueChangedListeners;

    // Constructors

    public ControlPropertyField(Context context) {
        super(context);
        initializePropertyField(context, null);
        initialize(context, null, 0);
        inflateView(context, null, 0);
    }

    public ControlPropertyField(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializePropertyField(context, attrs);
        initialize(context, attrs, 0);
        inflateView(context, null, 0);
    }

    public ControlPropertyField(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initializePropertyField(context, attrs);
        initialize(context, attrs, defStyle);
        inflateView(context, null, 0);
    }

    // Abstract methods

    protected abstract T getFieldValue();

    protected abstract void setFieldValue(T value);

    protected abstract void initialize(Context context, AttributeSet attrs, int defStyle);

    protected abstract void inflateView(Context context, AttributeSet attrs, int defStyle);

    protected abstract void requestFocusForContentView(View nextView);

    // Instance methods

    private void initializePropertyField(Context context, AttributeSet attrs) {
        caption = I18nProperties.getFieldCaption(getFieldCaptionPropertyId());
        description = I18nProperties.getFieldDescription(getFieldCaptionPropertyId());

        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.ControlPropertyField,
                    0, 0);

            try {
                showCaption = a.getBoolean(R.styleable.ControlPropertyField_showCaption, true);
                textAlignment = a.getInt(R.styleable.ControlPropertyField_textAlignment, View.TEXT_ALIGNMENT_VIEW_START);
                gravity = a.getInt(R.styleable.ControlPropertyField_gravity, Gravity.START | Gravity.CENTER_VERTICAL);
                imeOptions = a.getInt(R.styleable.ControlPropertyField_imeOptions, EditorInfo.IME_NULL);
                slim = a.getBoolean(R.styleable.ControlPropertyField_slim, false);
                if (a.hasValue(R.styleable.ControlPropertyField_captionCapitalized)) {
                    captionCapitalized = a.getBoolean(R.styleable.ControlPropertyField_captionCapitalized, true);
                } else {
                    captionCapitalized = null;
                }
                if (a.hasValue(R.styleable.ControlPropertyField_captionItalic)) {
                    captionItalic = a.getBoolean(R.styleable.ControlPropertyField_captionItalic, false);
                } else {
                    captionItalic = null;
                }
            } finally {
                a.recycle();
            }
        }
    }

    public void addValueChangedListener(ValueChangeListener listener) {
        if (valueChangedListeners == null) {
            valueChangedListeners = new ArrayList<>();
        }

        valueChangedListeners.add(listener);
    }

    protected void onValueChanged() {
        if (valueChangedListeners != null) {
            for (ValueChangeListener valueChangedListener : valueChangedListeners) {
                valueChangedListener.onChange(this);
            }
        }
    }

    private String getFieldIdString() {
        return getResources().getResourceName(getId());
    }

    public String getPropertyId() {
        String fieldId = getFieldIdString();
        int separatorIndex = fieldId.lastIndexOf("/");
        return fieldId.substring(separatorIndex + 1);
    }

    public String getFieldCaptionPropertyId() {
        String fieldId = getFieldIdString();
        return toPrefixPropertyId(fieldId);
    }

    public static String toPrefixPropertyId(String fieldId) {
        int separatorIndex = fieldId.lastIndexOf("/");
        return fieldId.substring(separatorIndex + 1, separatorIndex + 2).toUpperCase() + fieldId.substring(separatorIndex + 2).replaceAll("_", ".");
    }

    public String getPropertyIdWithoutPrefix() {
        String fieldId = getFieldIdString();
        int separatorIndex = fieldId.lastIndexOf("_");
        return fieldId.substring(separatorIndex + 1);
    }

    protected void setBackgroundResourceFor(View input, int resId) {
        int paddingLeft = input.getPaddingLeft();
        int paddingTop = input.getPaddingTop();
        int paddingRight = input.getPaddingRight();
        int paddingBottom = input.getPaddingBottom();

        input.setBackgroundResource(resId);

        input.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
    }

    protected void setBackgroundFor(View input, Drawable background) {
        int paddingLeft = input.getPaddingLeft();
        int paddingTop = input.getPaddingTop();
        int paddingRight = input.getPaddingRight();
        int paddingBottom = input.getPaddingBottom();

        input.setBackground(background);

        input.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
    }

    /**
     * Handles automatic visibility setting based on the dependencyParentField and
     * dependencyParentValue set in the layout.
     *
     * @param calledFromListener When fields are hidden, their value should generally be set to
     *                           null. However, this may only be done when this method is called
     *                           from the InternalValueChangedListener to prevent data loss in case
     *                           this method is called before the parent field's internal value
     *                           has been set
     */
    private void setVisibilityBasedOnParentField(boolean calledFromListener) {
        if (dependencyParentField == null || dependencyParentValue == null) {
            return;
        }

        if (dependencyParentField.getValue() == null ||
                dependencyParentField.getVisibility() != VISIBLE) {
            hideField(calledFromListener);
            return;
        }

        if (dependencyParentField.getValue() == dependencyParentValue) {
            if (dependencyParentVisibility) {
                setVisibility(VISIBLE);
            } else {
                hideField(calledFromListener);
            }
        } else {
            if (dependencyParentVisibility) {
                hideField(calledFromListener);
            } else {
                setVisibility(VISIBLE);
            }
        }
    }

    public void hideField(boolean eraseValue) {
        setVisibility(GONE);
        if (eraseValue) {
            setFieldValue(null);
        }
    }

    // Overrides

    @SuppressLint("WrongConstant")
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        labelFrame = this.findViewById(R.id.label_frame);
        label = (TextView) this.findViewById(R.id.label);

        if (label == null) {
            throw new NullPointerException("No label found for property field " + getFieldIdString());
        }

        label.setText(caption);
        label.setTextAlignment(textAlignment);

        if (getTextAlignment() == TEXT_ALIGNMENT_GRAVITY) {
            label.setGravity(getGravity());
        }

        // TODO: Refactor this after the tooltips component has been replaced
        label.setOnClickListener(new ControlLabelOnTouchListener(this));

        int typeFace = (captionItalic != null && captionItalic) ? Typeface.ITALIC : Typeface.NORMAL;

        if (captionCapitalized != null) {
            if (!captionCapitalized) {
                label.setTypeface(Typeface.create("sans-serif", typeFace));
                label.setAllCaps(false);
            } else {
                label.setTypeface(Typeface.create("sans-serif-medium", typeFace));
                label.setAllCaps(true);
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (labelFrame == null && label == null) {
            return;
        }

        int visible = isShowCaption() ? View.VISIBLE : View.GONE;

        if (labelFrame != null) {
            labelFrame.setVisibility(visible);
            return;
        }

        if (label != null) {
            label.setVisibility(visible);
        }
    }

    // Data binding, getters & setters

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;

        if (label == null) {
            throw new NullPointerException("No label found for property field " + getFieldIdString());
        }

        label.setText(caption);
    }

    public String getDescription() {
        return description;
    }

    @Override
    public int getTextAlignment() {
        return textAlignment;
    }

    @Override
    public int getGravity() {
        return gravity;
    }

    public int getImeOptions() {
        return imeOptions;
    }

    public void setImeOptions(int imeOptions) {
        this.imeOptions = imeOptions;
    }

    public boolean isShowCaption() {
        return showCaption;
    }

    public void setShowCaption(boolean showCaption) {
        this.showCaption = showCaption;
    }

    public boolean isSlim() {
        return slim;
    }

    public void setValue(Object value) {
        setFieldValue((T)value);
        onValueChanged();
    }

    public Object getValue() {
        return getFieldValue();
    }

    @BindingAdapter(value = {"dependencyParentField", "dependencyParentValue", "dependencyParentVisibility"}, requireAll = false)
    public static void setDependencyParentField(ControlPropertyField field, ControlPropertyField dependencyParentField, Object dependencyParentValue, Boolean dependencyParentVisibility) {
        field.dependencyParentField = dependencyParentField;
        field.dependencyParentValue = dependencyParentValue;

        if (dependencyParentVisibility != null) {
            field.dependencyParentVisibility = dependencyParentVisibility;
        }

        final ControlPropertyField thisField = field;
        if (dependencyParentField != null && dependencyParentValue != null) {
            thisField.setVisibilityBasedOnParentField(false);
            dependencyParentField.addValueChangedListener(new ValueChangeListener() {
                @Override
                public void onChange(ControlPropertyField field) {
                    thisField.setVisibilityBasedOnParentField(true);
                }
            });
        }
    }

}