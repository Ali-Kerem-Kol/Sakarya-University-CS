#include "stdio.h"
#include <stdio.h>
#include <stdlib.h>
#include "limits.h"
#include <unistd.h> // sleep(); için

#include "Habitat.h"
#include "Bitki.h"
#include "Pire.h"
#include "Sinek.h"

Habitat new_habitat(int row, int col)
{
    Habitat this;
    this = (Habitat)malloc(sizeof(struct HABITAT));
    this->row = row;
    this->col = col;

    this->canliMatris[row][col];
    this->turMatris[row][col];

    this->delete_habitat = &delete_habitat;
    this->habitat_simulasyon_baslat = &habitat_simulasyon_baslat;
    this->habitat_kazanan = &habitat_kazanan;
    this->yazdir = &yazdir;

    return this;
}


void delete_habitat(Habitat habitat) 
{
    if (habitat != NULL) 
    {
        // Canlıları ve matrisleri sırasıyla sil
        for (int i = 0; i < habitat->row; i++) 
        {
            for (int j = 0; j < habitat->col; j++) 
            {
                if (habitat->canliMatris[i][j] != NULL && habitat->turMatris != HICBIR) 
                {
                    // Canlıları sil
                    if (habitat->turMatris[i][j] == BITKI)
                    {
                        Bitki bitki = (Bitki)habitat->canliMatris[i][j];
                        bitki->sil(bitki);

                        habitat->canliMatris[i][j] = NULL;
                        habitat->turMatris[i][j] = HICBIR;
                    }
                    else if (habitat->turMatris[i][j] == BOCEK)
                    {
                        Bocek bocek = (Bocek)habitat->canliMatris[i][j];
                        bocek->sil(bocek);

                        habitat->canliMatris[i][j] = NULL;
                        habitat->turMatris[i][j] = HICBIR;
                    }
                    else if (habitat->turMatris[i][j] == SINEK)
                    {
                        Sinek sinek = (Sinek)habitat->canliMatris[i][j];
                        sinek->sil(sinek);

                        habitat->canliMatris[i][j] = NULL;
                        habitat->turMatris[i][j] = HICBIR;
                    }
                    else if (habitat->turMatris[i][j] == PIRE)
                    {
                        Pire pire = (Pire)habitat->canliMatris[i][j];
                        pire->sil(pire);

                        habitat->canliMatris[i][j] = NULL;
                        habitat->turMatris[i][j] = HICBIR;
                    }
                }
            }
            // Matrisi sil
            free(habitat->canliMatris[i]);
            free(habitat->turMatris[i]);
        }
        // Matrisin kendisini sil
        free(habitat->canliMatris);
        free(habitat->turMatris);
        // Habitat yapısını sil
        free(habitat);
    }
}

void yazdir(const Habitat this)
{
    printf("\n");

    for (int i = 0; i < this->row; i++)
    {
        for (int j = 0; j < this->col; j++)
        {
            if (this->canliMatris[i][j] != NULL)
            {
                if (this->turMatris[i][j] == BITKI)
                {
                    Bitki bitki = (Bitki)this->canliMatris[i][j];
                    printf("|%s ", bitki->super->gorunum());
                }
                else if (this->turMatris[i][j] == BOCEK)
                {
                    Bocek bocek = (Bocek)this->canliMatris[i][j];
                    printf("|%s ", bocek->super->gorunum());
                }
                else if (this->turMatris[i][j] == SINEK)
                {
                    Sinek sinek = (Sinek)this->canliMatris[i][j];
                    printf("|%s ", sinek->supersuper->gorunum());
                }
                else if (this->turMatris[i][j] == PIRE)
                {
                    Pire pire = (Pire)this->canliMatris[i][j];
                    printf("|%s ", pire->supersuper->gorunum());
                }
                else if (this->turMatris[i][j] == HICBIR)
                {
                    printf("| X");
                }
            }
            else
            {
                printf("| X");
            }
        }
        printf("|\n");
    }
    printf("\n");
}

void habitat_simulasyon_baslat(Habitat habitat)
{
    int sutun = 0;
    for (int i = 0; i < habitat->row; i++)
    {
        sutun = 0;
        for (int j = 0; j < habitat->col; j++)
        {

            if (habitat->canliMatris[i][j] != NULL)
            {

                if (habitat->turMatris[i][j] == BITKI) //<<<<<<<<<<<<<<<<<<<<<<<
                {
                    for (int a = 0; a < habitat->row; a++)
                    {
                        for (int b = 0; b < habitat->col; b++)
                        {

                            if (habitat->turMatris[a][b] == PIRE)
                            {

                                Pire pire = (Pire)habitat->canliMatris[a][b];
                                pire->sil(pire);

                                habitat->canliMatris[a][b] = NULL;
                                habitat->turMatris[a][b] = HICBIR;

                                system("cls");
                                yazdir(habitat);
                                //sleep(1);
                            }
                            else if (habitat->turMatris[a][b] == SINEK)
                            {

                                Sinek sinek = (Sinek)habitat->canliMatris[a][b];
                                sinek->sil(sinek);

                                habitat->canliMatris[a][b] = NULL;
                                habitat->turMatris[a][b] = HICBIR;

                                system("cls");
                                yazdir(habitat);
                                //sleep(1);
                            }
                            else if (habitat->turMatris[a][b] == BOCEK && habitat->turMatris[i][j] == BITKI)
                            {

                                Bitki bitki = (Bitki)habitat->canliMatris[i][j];
                                bitki->sil(bitki);

                                habitat->canliMatris[i][j] = NULL;
                                habitat->turMatris[i][j] = HICBIR;

                                system("cls");
                                yazdir(habitat);
                                //sleep(1);

                                a = habitat->row + 1;
                                b = habitat->col + 1;
                            }
                            else if (habitat->turMatris[a][b] == BITKI) //<<<<
                            {

                                Bitki ilkbitki = (Bitki)habitat->canliMatris[i][j];
                                Bitki sonrakibitki = (Bitki)habitat->canliMatris[a][b];
                                if (ilkbitki->super->value > sonrakibitki->super->value)
                                {
                                    sonrakibitki->sil(sonrakibitki);

                                    habitat->canliMatris[a][b] = NULL;
                                    habitat->turMatris[a][b] = HICBIR;

                                    system("cls");
                                    yazdir(habitat);
                                    //sleep(1);
                                }
                                else if (ilkbitki->super->value < sonrakibitki->super->value)
                                {
                                    ilkbitki->sil(ilkbitki);

                                    habitat->canliMatris[i][j] = NULL;
                                    habitat->turMatris[i][j] = HICBIR;

                                    system("cls");
                                    yazdir(habitat);
                                    //sleep(1);

                                    a = habitat->row + 1;
                                    b = habitat->col + 1;
                                }
                                else if (ilkbitki->super->value == sonrakibitki->super->value)
                                {

                                    if (a > i)
                                    {
                                        sonrakibitki->sil(sonrakibitki);

                                        habitat->canliMatris[a][b] = NULL;
                                        habitat->turMatris[a][b] = HICBIR;

                                        system("cls");
                                        yazdir(habitat);
                                        //sleep(1);
                                    }
                                    else if (a < i)
                                    {
                                        ilkbitki->sil(ilkbitki);

                                        habitat->canliMatris[i][j] = NULL;
                                        habitat->turMatris[i][j] = HICBIR;

                                        system("cls");
                                        yazdir(habitat);
                                        //sleep(1);

                                        a = habitat->row + 1;
                                        b = habitat->col + 1;
                                    }
                                    else if (a == i && b > j)
                                    {
                                        sonrakibitki->sil(sonrakibitki);

                                        habitat->canliMatris[a][b] = NULL;
                                        habitat->turMatris[a][b] = HICBIR;

                                        system("cls");
                                        yazdir(habitat);
                                        //sleep(1);
                                    }
                                    else if (a == i && b < j)
                                    {
                                        ilkbitki->sil(ilkbitki);

                                        habitat->canliMatris[i][j] = NULL;
                                        habitat->turMatris[i][j] = HICBIR;

                                        system("cls");
                                        yazdir(habitat);
                                        //sleep(1);

                                        a = habitat->row + 1;
                                        b = habitat->col + 1;
                                    }
                                }
                            }
                        }
                    }
                }
                else if (habitat->turMatris[i][j] == BOCEK) //<<<<<<<<<<<<<<<<<<<<<<<
                {
                    for (int a = 0; a < habitat->row; a++)
                    {
                        for (int b = 0; b < habitat->col; b++)
                        {

                            if (habitat->turMatris[a][b] == BITKI)
                            {

                                Bitki bitki = (Bitki)habitat->canliMatris[a][b];
                                bitki->sil(bitki);

                                habitat->canliMatris[a][b] = NULL;
                                habitat->turMatris[a][b] = HICBIR;

                                system("cls");
                                yazdir(habitat);
                                //sleep(1);
                            }
                            else if (habitat->turMatris[a][b] == PIRE)
                            {

                                Pire pire = (Pire)habitat->canliMatris[a][b];
                                pire->sil(pire);

                                habitat->canliMatris[a][b] = NULL;
                                habitat->turMatris[a][b] = HICBIR;

                                system("cls");
                                yazdir(habitat);
                                //sleep(1);
                            }
                            else if (habitat->turMatris[a][b] == SINEK && habitat->turMatris[i][j] == BOCEK)
                            {

                                Bocek bocek = (Bocek)habitat->canliMatris[i][j];
                                bocek->sil(bocek);

                                habitat->canliMatris[i][j] = NULL;
                                habitat->turMatris[i][j] = HICBIR;

                                system("cls");
                                yazdir(habitat);
                                //sleep(1);

                                a = habitat->row + 1;
                                b = habitat->col + 1;
                            }
                            else if (habitat->turMatris[a][b] == BOCEK) //<<<<
                            {

                                if ((Bocek)habitat->canliMatris[i][j]->value > (Bocek)habitat->canliMatris[a][b]->value)
                                {

                                    Bocek bocek = (Bocek)habitat->canliMatris[a][b];
                                    bocek->sil(bocek);

                                    habitat->canliMatris[a][b] = NULL;
                                    habitat->turMatris[a][b] = HICBIR;

                                    system("cls");
                                    yazdir(habitat);
                                    //sleep(1);
                                }
                                else if ((Bocek)habitat->canliMatris[i][j]->value < (Bocek)habitat->canliMatris[a][b]->value)
                                {

                                    Bocek bocek = (Bocek)habitat->canliMatris[i][j];
                                    bocek->sil(bocek);

                                    habitat->canliMatris[i][j] = NULL;
                                    habitat->turMatris[i][j] = HICBIR;

                                    system("cls");
                                    yazdir(habitat);
                                    //sleep(1);

                                    a = habitat->row + 1;
                                    b = habitat->col + 1;
                                }
                                else if ((Bocek)habitat->canliMatris[i][j]->value == (Bocek)habitat->canliMatris[a][b]->value)
                                {

                                    if (a > i)
                                    {

                                        Bocek bocek = (Bocek)habitat->canliMatris[a][b];
                                        bocek->sil(bocek);

                                        habitat->canliMatris[a][b] = NULL;
                                        habitat->turMatris[a][b] = HICBIR;

                                        system("cls");
                                        yazdir(habitat);
                                        //sleep(1);
                                    }
                                    else if (a < i)
                                    {

                                        Bocek bocek = (Bocek)habitat->canliMatris[i][j];
                                        bocek->sil(bocek);

                                        habitat->canliMatris[i][j] = NULL;
                                        habitat->turMatris[i][j] = HICBIR;

                                        system("cls");
                                        yazdir(habitat);
                                        //sleep(1);

                                        a = habitat->row + 1;
                                        b = habitat->col + 1;
                                    }
                                    else if (a == i && b > j)
                                    {

                                        Bocek bocek = (Bocek)habitat->canliMatris[a][b];
                                        bocek->sil(bocek);

                                        habitat->canliMatris[a][b] = NULL;
                                        habitat->turMatris[a][b] = HICBIR;

                                        system("cls");
                                        yazdir(habitat);
                                        //sleep(1);
                                    }
                                    else if (a == i && b < j)
                                    {

                                        Bocek bocek = (Bocek)habitat->canliMatris[i][j];
                                        bocek->sil(bocek);

                                        habitat->canliMatris[i][j] = NULL;
                                        habitat->turMatris[i][j] = HICBIR;

                                        system("cls");
                                        yazdir(habitat);
                                        //sleep(1);

                                        a = habitat->row + 1;
                                        b = habitat->col + 1;
                                    }
                                }
                            }
                        }
                    }
                }
                else if (habitat->turMatris[i][j] == SINEK) //<<<<<<<<<<<<<<<<<<<<<<<
                {
                    for (int a = 0; a < habitat->row; a++)
                    {
                        for (int b = 0; b < habitat->col; b++)
                        {

                            if (habitat->turMatris[a][b] == PIRE)
                            {

                                Pire pire = (Pire)habitat->canliMatris[a][b];
                                pire->sil(pire);

                                habitat->canliMatris[a][b] = NULL;
                                habitat->turMatris[a][b] = HICBIR;

                                system("cls");
                                yazdir(habitat);
                                //sleep(1);
                            }
                            else if (habitat->turMatris[a][b] == BOCEK)
                            {

                                Bocek bocek = (Bocek)habitat->canliMatris[a][b];
                                bocek->sil(bocek);

                                habitat->canliMatris[a][b] = NULL;
                                habitat->turMatris[a][b] = HICBIR;

                                system("cls");
                                yazdir(habitat);
                                //sleep(1);
                            }
                            else if (habitat->turMatris[a][b] == BITKI && habitat->turMatris[i][j] == SINEK)
                            {

                                Sinek sinek = (Sinek)habitat->canliMatris[i][j];
                                sinek->sil(sinek);

                                habitat->canliMatris[i][j] = NULL;
                                habitat->turMatris[i][j] = HICBIR;

                                system("cls");
                                yazdir(habitat);
                                //sleep(1);

                                a = habitat->row + 1;
                                b = habitat->col + 1;
                            }
                            else if (habitat->turMatris[a][b] == SINEK) //<<<<
                            {

                                if ((Sinek)habitat->canliMatris[i][j]->value > (Sinek)habitat->canliMatris[a][b]->value)
                                {

                                    Sinek sinek = (Sinek)habitat->canliMatris[a][b];
                                    sinek->sil(sinek);

                                    habitat->canliMatris[a][b] = NULL;
                                    habitat->turMatris[a][b] = HICBIR;

                                    system("cls");
                                    yazdir(habitat);
                                    //sleep(1);
                                }
                                else if ((Sinek)habitat->canliMatris[i][j]->value < (Sinek)habitat->canliMatris[a][b]->value)
                                {

                                    Sinek sinek = (Sinek)habitat->canliMatris[i][j];
                                    sinek->sil(sinek);

                                    habitat->canliMatris[i][j] = NULL;
                                    habitat->turMatris[i][j] = HICBIR;

                                    system("cls");
                                    yazdir(habitat);
                                    //sleep(1);

                                    a = habitat->row + 1;
                                    b = habitat->col + 1;
                                }
                                else if ((Sinek)habitat->canliMatris[i][j]->value == (Sinek)habitat->canliMatris[a][b]->value)
                                {

                                    if (a > i)
                                    {

                                        Sinek sinek = (Sinek)habitat->canliMatris[a][b];
                                        sinek->sil(sinek);

                                        habitat->canliMatris[a][b] = NULL;
                                        habitat->turMatris[a][b] = HICBIR;

                                        system("cls");
                                        yazdir(habitat);
                                        //sleep(1);
                                    }
                                    else if (a < i)
                                    {

                                        Sinek sinek = (Sinek)habitat->canliMatris[i][j];
                                        sinek->sil(sinek);

                                        habitat->canliMatris[i][j] = NULL;
                                        habitat->turMatris[i][j] = HICBIR;

                                        system("cls");
                                        yazdir(habitat);
                                        //sleep(1);

                                        a = habitat->row + 1;
                                        b = habitat->col + 1;
                                    }
                                    else if (a == i && b > j)
                                    {

                                        Sinek sinek = (Sinek)habitat->canliMatris[a][b];
                                        sinek->sil(sinek);

                                        habitat->canliMatris[a][b] = NULL;
                                        habitat->turMatris[a][b] = HICBIR;

                                        system("cls");
                                        yazdir(habitat);
                                        //sleep(1);
                                    }
                                    else if (a == i && b < j)
                                    {

                                        Sinek sinek = (Sinek)habitat->canliMatris[i][j];
                                        sinek->sil(sinek);

                                        habitat->canliMatris[i][j] = NULL;
                                        habitat->turMatris[i][j] = HICBIR;

                                        system("cls");
                                        yazdir(habitat);
                                        //sleep(1);

                                        a = habitat->row + 1;
                                        b = habitat->col + 1;
                                    }
                                }
                            }
                        }
                    }
                }
                else if (habitat->turMatris[i][j] == PIRE) //<<<<<<<<<<<<<<<<<<<<<<<
                {
                    for (int a = 0; a < habitat->row; a++)
                    {
                        for (int b = 0; b < habitat->col; b++)
                        {

                            if (habitat->turMatris[a][b] == BITKI && habitat->turMatris[i][j] == PIRE)
                            {

                                Pire pire = (Pire)habitat->canliMatris[i][j];
                                pire->sil(pire);

                                habitat->canliMatris[i][j] = NULL;
                                habitat->turMatris[i][j] = HICBIR;

                                system("cls");
                                yazdir(habitat);
                                //sleep(1);

                                a = habitat->row + 1;
                                b = habitat->col + 1;
                            }
                            else if (habitat->turMatris[a][b] == SINEK && habitat->turMatris[i][j] == PIRE)
                            {

                                Pire pire = (Pire)habitat->canliMatris[i][j];
                                pire->sil(pire);

                                habitat->canliMatris[i][j] = NULL;
                                habitat->turMatris[i][j] = HICBIR;

                                system("cls");
                                yazdir(habitat);
                                //sleep(1);

                                a = habitat->row + 1;
                                b = habitat->col + 1;
                            }
                            else if (habitat->turMatris[a][b] == BOCEK && habitat->turMatris[i][j] == PIRE)
                            {

                                Pire pire = (Pire)habitat->canliMatris[i][j];
                                pire->sil(pire);

                                habitat->canliMatris[i][j] = NULL;
                                habitat->turMatris[i][j] = HICBIR;

                                system("cls");
                                yazdir(habitat);
                                //sleep(1);

                                a = habitat->row + 1;
                                b = habitat->col + 1;
                            }
                            else if (habitat->turMatris[a][b] == PIRE) //<<<<
                            {

                                if ((Pire)habitat->canliMatris[i][j]->value > (Pire)habitat->canliMatris[a][b]->value)
                                {

                                    Pire pire = (Pire)habitat->canliMatris[a][b];
                                    pire->sil(pire);

                                    habitat->canliMatris[a][b] = NULL;
                                    habitat->turMatris[a][b] = HICBIR;

                                    system("cls");
                                    yazdir(habitat);
                                    //sleep(1);
                                }
                                else if ((Pire)habitat->canliMatris[i][j]->value < (Pire)habitat->canliMatris[a][b]->value)
                                {

                                    Pire pire = (Pire)habitat->canliMatris[i][j];
                                    pire->sil(pire);

                                    habitat->canliMatris[i][j] = NULL;
                                    habitat->turMatris[i][j] = HICBIR;

                                    system("cls");
                                    yazdir(habitat);
                                    //sleep(1);

                                    a = habitat->row + 1;
                                    b = habitat->col + 1;
                                }
                                else if ((Pire)habitat->canliMatris[i][j]->value == (Pire)habitat->canliMatris[a][b]->value)
                                {

                                    if (a > i)
                                    {

                                        Pire pire = (Pire)habitat->canliMatris[a][b];
                                        pire->sil(pire);

                                        habitat->canliMatris[a][b] = NULL;
                                        habitat->turMatris[a][b] = HICBIR;

                                        system("cls");
                                        yazdir(habitat);
                                        //sleep(1);
                                    }
                                    else if (a < i)
                                    {

                                        Pire pire = (Pire)habitat->canliMatris[i][j];
                                        pire->sil(pire);

                                        habitat->canliMatris[i][j] = NULL;
                                        habitat->turMatris[i][j] = HICBIR;

                                        system("cls");
                                        yazdir(habitat);
                                        //sleep(1);

                                        a = habitat->row + 1;
                                        b = habitat->col + 1;
                                    }
                                    else if (a == i && b > j)
                                    {

                                        Pire pire = (Pire)habitat->canliMatris[a][b];
                                        pire->sil(pire);

                                        habitat->canliMatris[a][b] = NULL;
                                        habitat->turMatris[a][b] = HICBIR;

                                        system("cls");
                                        yazdir(habitat);
                                        //sleep(1);
                                    }
                                    else if (a == i && b < j)
                                    {

                                        Pire pire = (Pire)habitat->canliMatris[i][j];
                                        pire->sil(pire);

                                        habitat->canliMatris[i][j] = NULL;
                                        habitat->turMatris[i][j] = HICBIR;

                                        system("cls");
                                        yazdir(habitat);
                                        //sleep(1);

                                        a = habitat->row + 1;
                                        b = habitat->col + 1;
                                    }
                                }
                            }
                        }
                    }
                }
                else
                {
                    
                }
            }
        }
    }
}

void habitat_kazanan(struct HABITAT *this)
{
    printf("\n");

    for (int i = 0; i < this->row; i++)
    {
        for (int j = 0; j < this->col; j++)
        {
            if (this->canliMatris[i][j] != NULL)
            {
                if (this->turMatris[i][j] == BITKI)
                {
                    Bitki bitki = (Bitki)this->canliMatris[i][j];
                    printf("Kazanan: ");
                    printf("%s", bitki->super->gorunum());
                    printf(" : ( %d,", i);
                    printf(" %d)", j);
                }
                else if (this->turMatris[i][j] == BOCEK)
                {
                    Bocek bocek = (Bocek)this->canliMatris[i][j];
                    printf("Kazanan: ");
                    printf("%s", bocek->super->gorunum());
                    printf(" : ( %d,", i);
                    printf(" %d)", j);
                }
                else if (this->turMatris[i][j] == SINEK)
                {
                    Sinek sinek = (Sinek)this->canliMatris[i][j];
                    printf("Kazanan: ");
                    printf("%s", sinek->supersuper->gorunum());
                    printf(" : ( %d,", i);
                    printf(" %d)", j);
                }
                else if (this->turMatris[i][j] == PIRE)
                {
                    Pire pire = (Pire)this->canliMatris[i][j];
                    printf("Kazanan: ");
                    printf("%s", pire->supersuper->gorunum());
                    printf(" : ( %d,", i);
                    printf(" %d)", j);
                }
            }
        }
    }
}
