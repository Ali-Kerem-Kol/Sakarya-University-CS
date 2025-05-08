/**
* @file B221210042
* @description Yığın Sınıfının Başlık Dosyası.
* @course YouTube : MFA ve YouTube : Kayhan Ayar
* @assignment 2.Ödev
* @date 14/12/2023
* @author Ali Kerem Kol , E-Posta : ali.kol@ogr.sakarya.edu.tr
*/


#ifndef YIGIN_HPP
#define YIGIN_HPP

class Yigin {
private:
    int tepe;
    int kapasite;
    int* veriler;

    void genislet(int boyut);

public:
    Yigin();
    void Push(int veri);
    void Pop();
    bool BosMu();
    int getir();
    void clear();
    ~Yigin();
};

#endif
