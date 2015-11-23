package net.bierbaumer.otp_authenticator;

import android.content.ClipData;
import android.graphics.Color;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;


public class EntriesAdapter extends BaseAdapter {
    private List<Entry> entries;
    private Entry currentSelection;


    @Override
    public int getCount() {
        return getEntries().size();
    }

    @Override
    public Entry getItem(int i) {
        return getEntries().get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            final LayoutInflater vi;
            vi = LayoutInflater.from(parent.getContext());
            v = vi.inflate(R.layout.row, null);
        }

        v.setBackgroundColor(Color.TRANSPARENT);

        if(getEntries().get(position) == getCurrentSelection()){
            v.setBackgroundColor(parent.getResources().getColor(R.color.primary_light));
        }



        final TextView tt1 = (TextView) v.findViewById(R.id.textViewLabel);
        tt1.setText(getItem(position).getLabel());

        TextView tt2 = (TextView) v.findViewById(R.id.textViewOTP);
        v.setTag(position);
        tt2.setText(getItem(position).getCurrentOTP());


        v.setOnDragListener(new View.OnDragListener() {

            @Override
            public boolean onDrag(View v, DragEvent event) {

                final int action = event.getAction();
                switch (action) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        break;

                    case DragEvent.ACTION_DRAG_EXITED:
                        break;

                    case DragEvent.ACTION_DRAG_ENTERED:
                        break;

                    case DragEvent.ACTION_DROP: {
                        int from = Integer.parseInt(event.getClipData().getDescription().getLabel()+"");
                        int to = (Integer) v.getTag();
                        Entry e = getEntries().remove(from);
                        getEntries().add(to, e);
                        notifyDataSetChanged();

                        return true;
                    }

                    case DragEvent.ACTION_DRAG_ENDED: {
                        return true;
                    }

                    default:
                        break;
                }
                return true;
            }
        });
        v.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent arg1) {

                if (getCurrentSelection() != getEntries().get(position)) {
                    return false;
                }

                ClipData data = ClipData.newPlainText(v.getTag() + "", "");
                View.DragShadowBuilder shadow = new View.DragShadowBuilder(v);
                v.startDrag(data, shadow, null, 0);

                return false;
            }
        });

        return v;
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public void setEntries(List<Entry> entries) {
        this.entries = entries;
    }

    public Entry getCurrentSelection() {
        return currentSelection;
    }

    public void setCurrentSelection(Entry currentSelection) {
        this.currentSelection = currentSelection;
    }
}