package de.reiss.xprivacynative.nativesdcard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import biz.bokhorst.xprivacy.R;

import java.util.ArrayList;
import java.util.Collection;

public class FileObserveTaskListAdapter extends ArrayAdapter<FileObserveTask> {
    private final Context context;

    public ArrayList<FileObserveTask> tasks;

    public FileObserveTaskListAdapter(Context context, ArrayList<FileObserveTask> tasks) {
        super(context, R.layout.filewatchlistitem, tasks);
        this.context = context;
        this.tasks = tasks;
    }

    static class ViewHolder {
        protected TextView filewatchlistitem_tv_thread;
        protected Button filewatchlistitem_btn_stop;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inf = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inf.inflate(R.layout.filewatchlistitem, null);
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.filewatchlistitem_tv_thread = (TextView)
                convertView.findViewById(R.id.filewatchlistitem_tv_thread);

        viewHolder.filewatchlistitem_btn_stop = (Button)
                convertView.findViewById(R.id.filewatchlistitem_btn_stop
                );
        viewHolder.filewatchlistitem_btn_stop.setText(
                viewHolder.filewatchlistitem_btn_stop.getText().toString());
        viewHolder.filewatchlistitem_btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    FileObserveTask regardedTask = getItem(position);
                    regardedTask.stopWatching();
                    remove(regardedTask);
                    notifyDataSetChanged();
                    final String toastText = "Stopped file watcher for file '"
                            + regardedTask.fileToObserve + "'";
                    Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show();
                } catch (IndexOutOfBoundsException e) {
                    // shouldn't happen
                    e.printStackTrace();
                }
            }
        });
        viewHolder.filewatchlistitem_tv_thread.setText(tasks.get(position).toString());
        return convertView;
    }


    @Override
    public void add(FileObserveTask object) {
        tasks.add(object);
        notifyDataSetChanged();
    }

    @Override
    public void addAll(Collection<? extends FileObserveTask> collection) {
        tasks.addAll(collection);
        notifyDataSetChanged();
    }

    @Override
    public void remove(FileObserveTask object) {
        tasks.remove(object);
        notifyDataSetChanged();
    }

    @Override
    public void insert(FileObserveTask object, int index) {
        tasks.add(index, object);
        notifyDataSetChanged();
    }

    @Override
    public void clear() {
        tasks.clear();
        notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }
}