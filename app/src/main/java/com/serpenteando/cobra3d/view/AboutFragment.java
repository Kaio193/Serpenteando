package com.serpenteando.cobra3d.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.serpenteando.cobra3d.R;

public class AboutFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Botão voltar
        view.findViewById(R.id.backButton).setOnClickListener(vista ->
                Navigation.findNavController(view).popBackStack());

        // [TEI Intents] LinkedIn e e-mail de cada integrante
        // (Intent implícita — mesma ideia do "Fale Conosco")
        abrirLink(view, R.id.linkedinKaio, "https://www.linkedin.com/in/kaio-dantas193/");
        abrirLink(view, R.id.instagramKaio, "https://www.instagram.com/kaio.russoo/");
        abrirEmail(view, R.id.emailKaio, "kaiodm193@gmail.com");

        abrirLink(view, R.id.linkedinEbert, "https://www.linkedin.com/in/ebert-souza-b7925840a/");
        abrirLink(view, R.id.instagramEbert, "https://www.instagram.com/ebert.damasceno/");
        abrirEmail(view, R.id.emailEbert, "souzaebert25@gmail.com");

        abrirLink(view, R.id.linkedinAlexandre, "https://www.linkedin.com/in/alexandrecostapb/");
        abrirLink(view, R.id.instagramAlexandre, "https://www.instagram.com/alexandrecostapb/");
        abrirEmail(view, R.id.emailAlexandre, "costa.alexandre@ifpb.edu.br");

        abrirLink(view, R.id.linkedinFelipe, "https://www.linkedin.com/in/felipe-ramos-b480b78a/");
        abrirLink(view, R.id.instagramFelipe, "https://www.instagram.com/felipebar15/");
        abrirEmail(view, R.id.emailFelipe, "felipe.ramos@ifpb.edu.br");

        abrirLink(view, R.id.instagramPaulo, "https://www.instagram.com/paulo.ragner/");
        abrirEmail(view, R.id.emailPaulo, "paulo.freitas@ifpb.edu.br");

        // Ajusta o corte das fotos pra não cortar o cabelo de quem mandou foto
        // vertical (a quadrada não muda nada)
        view.post(() -> {
            aplicarEnquadramento(view, R.id.profileKaio);
            aplicarEnquadramento(view, R.id.profileEbert);
            aplicarEnquadramento(view, R.id.profileAlexandre);
            aplicarEnquadramento(view, R.id.profileFelipe);
            aplicarEnquadramento(view, R.id.profilePaulo);
        });
    }

    // Abre o link no navegador ou no app do sistema
    private void abrirLink(View raiz, int id, String url) {
        View icone = raiz.findViewById(id);
        if (icone == null) return; // integrante sem esse dado cadastrado
        icone.setOnClickListener(vista -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            } catch (Exception e) {
                Toast.makeText(getContext(), R.string.error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Abre o e-mail já com o destinatário preenchido
    private void abrirEmail(View raiz, int id, String email) {
        View icone = raiz.findViewById(id);
        if (icone == null) return;
        icone.setOnClickListener(vista -> {
            try {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:" + email));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(getContext(), R.string.settings_no_email, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Corta a foto no estilo centerCrop, mas puxando o corte pra cima pra
    // não cortar o cabelo (bias 0 = topo, 0.5 = centro)
    private void aplicarEnquadramento(View raiz, int id, float bias) {
        ImageView iv = raiz.findViewById(id);
        if (iv == null || iv.getWidth() == 0 || iv.getHeight() == 0) return;
        android.graphics.drawable.Drawable d = iv.getDrawable();
        if (d == null) return;

        float lv = iv.getWidth(), av = iv.getHeight();
        float lf = d.getIntrinsicWidth(), af = d.getIntrinsicHeight();
        if (lf <= 0 || af <= 0) return;

        // Mesma escala do centerCrop (cobre a view inteira)
        float escala = Math.max(lv / lf, av / af);
        float dx = (lv - lf * escala) * 0.5f;
        float dy = (av - af * escala) * bias;

        android.graphics.Matrix m = new android.graphics.Matrix();
        m.setScale(escala, escala);
        m.postTranslate(dx, dy);
        iv.setScaleType(ImageView.ScaleType.MATRIX);
        iv.setImageMatrix(m);
    }

    private void aplicarEnquadramento(View raiz, int id) {
        aplicarEnquadramento(raiz, id, 0.15f);
    }
}
