package com.example.pspassistant.Adaptadores;


import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.pspassistant.Modelos.Proyecto;
import com.example.pspassistant.R;

import java.util.List;

public class Adaptador_Proyecto extends RecyclerView.Adapter<Adaptador_Proyecto.ViewHolder> {

    private List<Proyecto> Proyectos;
    private int Layout;
    private OnItemClickListener onItemClickListener;
    private OnLongClickListener onLongClickListener;

    public Adaptador_Proyecto(List<Proyecto> Proyectos, int Layout, OnItemClickListener onItemClickListener, OnLongClickListener onLongClickListener){
        this.Proyectos = Proyectos;
        this.Layout = Layout;
        this.onItemClickListener = onItemClickListener;
        this.onLongClickListener = onLongClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(Layout, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.Pintar(Proyectos.get(i), onItemClickListener, onLongClickListener);
    }

    @Override
    public int getItemCount() {
        return Proyectos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView Nombre;
        public TextView Lenguaje;
        public TextView Fecha;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.Nombre = (TextView) itemView.findViewById(R.id.Nombre_proyecto);
            this.Lenguaje = (TextView) itemView.findViewById(R.id.Lenguaje);
            this.Fecha = (TextView) itemView.findViewById(R.id.Fecha_creacion);
        }

        public void Pintar(final Proyecto proyecto, final OnItemClickListener onItemClickListener, final OnLongClickListener onLongClickListener){
            this.Nombre.setText(proyecto.getNombre_Proyecto());
            this.Lenguaje.setText(proyecto.getLenguaje());
            this.Fecha.setText(proyecto.getFecha_Creacion());

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.OnClick(proyecto, getAdapterPosition());
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onLongClickListener.OnLongClick(proyecto, getAdapterPosition());
                    return true;
                }
            });

        }
    }

    public interface OnItemClickListener{
        void OnClick(Proyecto proyecto, int posicion);
    }

    public interface OnLongClickListener{
        void OnLongClick(Proyecto proyecto, int posicion);
    }
}
