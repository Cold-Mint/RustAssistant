package com.muqing;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;
public class VH<Binging extends ViewBinding> extends RecyclerView.ViewHolder {

    public Binging binging;
    public VH(@NonNull Binging itemView) {
        super(itemView.getRoot());
        this.binging = itemView;
    }
}
