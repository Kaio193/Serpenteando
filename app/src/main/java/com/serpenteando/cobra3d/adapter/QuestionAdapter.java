package com.serpenteando.cobra3d.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.serpenteando.cobra3d.R;
import com.serpenteando.cobra3d.model.entity.Question;

import java.util.List;

// [TEI RecyclerView] Adapter do quiz — conecta a lista de perguntas ao RecyclerView
public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.ViewHolder> {

    public interface OnAnswerSelectedListener {
        void onAnswerSelected(int answerIndex);
    }

    private List<Question> questions;
    private OnAnswerSelectedListener listener;
    private int selectedPosition = -1;
    private boolean enabled = true;

    public QuestionAdapter(List<Question> questions, OnAnswerSelectedListener listener) {
        this.questions = questions;
        this.listener = listener;
    }

    // [TEI RecyclerView] onCreateViewHolder: infla o layout de cada item da lista
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_question, parent, false);
        return new ViewHolder(view);
    }

    // [TEI RecyclerView] onBindViewHolder: preenche cada item com dados da pergunta
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Question question = questions.get(position);
        holder.questionText.setText(question.getQuestionText());

        holder.radioGroup.setOnCheckedChangeListener(null);
        holder.radioGroup.clearCheck();

        holder.optionA.setText(question.getOptionA());
        holder.optionB.setText(question.getOptionB());
        holder.optionC.setText(question.getOptionC());
        holder.optionD.setText(question.getOptionD());

        holder.radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (!enabled) return;
            int index = -1;
            if (checkedId == R.id.radioA) index = 0;
            else if (checkedId == R.id.radioB) index = 1;
            else if (checkedId == R.id.radioC) index = 2;
            else if (checkedId == R.id.radioD) index = 3;

            selectedPosition = index;
            if (listener != null) {
                listener.onAnswerSelected(index);
            }
        });

        holder.itemView.setAlpha(enabled ? 1.0f : 0.5f);
    }

    // [TEI RecyclerView] getItemCount: informa o total de itens para o RecyclerView
    @Override
    public int getItemCount() {
        return questions != null ? questions.size() : 0;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        notifyDataSetChanged();
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    // [TEI RecyclerView] ViewHolder: guarda referências dos widgets para evitar findViewById repetido
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView questionText;
        RadioGroup radioGroup;
        RadioButton optionA, optionB, optionC, optionD;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            questionText = itemView.findViewById(R.id.questionText);
            radioGroup = itemView.findViewById(R.id.radioGroup);
            optionA = itemView.findViewById(R.id.radioA);
            optionB = itemView.findViewById(R.id.radioB);
            optionC = itemView.findViewById(R.id.radioC);
            optionD = itemView.findViewById(R.id.radioD);
        }
    }
}
