#ifndef MAINWINDOW_H
#define MAINWINDOW_H

#include <QMainWindow>
#include <QtNetwork>
#include "basexthread.h"

namespace Ui {
    class MainWindow;
}

class MainWindow : public QMainWindow
{
    Q_OBJECT

public:
    explicit MainWindow(QWidget *parent = 0);
    ~MainWindow();

private:
    Ui::MainWindow *ui;
    BaseXThread * bxth;

private slots:
     void displayError(int socketError, const QString &errorString);

     void basexFeedBack(const QByteArray * array);
     void buttonDo();



};

#endif // MAINWINDOW_H
