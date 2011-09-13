#include "mainwindow.h"
#include "ui_mainwindow.h"
#include <QtGui>


MainWindow::MainWindow(QWidget *parent) :
    QMainWindow(parent),
    ui(new Ui::MainWindow)
{
    ui->setupUi(this);

    QString host("localhost");
    bxth = new BaseXThread(host,1984, this);


    connect(bxth, SIGNAL(socketError(int,const QString&)), SLOT(displayError(int,const QString&)));
    connect(ui->doItButton, SIGNAL(clicked()), SLOT(buttonDo()));
    connect(bxth,SIGNAL(newResult(const QByteArray*)),SLOT(basexFeedBack(const QByteArray*)));

}

void MainWindow::displayError(int socketError, const QString &errorString)
{
    switch (socketError) {
    case QAbstractSocket::RemoteHostClosedError:
        break;
    case QAbstractSocket::HostNotFoundError:
        QMessageBox::information(this, tr("BaseX Client"),
                                 tr("The host was not found. Please check the "
                                    "host name and port settings."));
        break;
    case QAbstractSocket::ConnectionRefusedError:
        QMessageBox::information(this, tr("BaseX Client"),
                                 tr("The connection was refused by the peer. "
                                    "Make sure the BaseX server is running, "
                                    "and check that the host name and port "
                                    "settings are correct."));
        break;
    default:
        QMessageBox::information(this, tr("BaseX Client"),
                                 tr("The following error occurred:\n %1.")
                                 .arg(errorString));
    }
}

MainWindow::~MainWindow()
{
    delete ui;
}

void MainWindow::basexFeedBack(const QByteArray *array)
{

   QString abc(ui->report->toPlainText());
   abc.append(*array);
   ui->report->setPlainText(abc);
   delete array;

}

void MainWindow::buttonDo()
{

    QString fileName = QFileDialog::getOpenFileName(this,
         tr("Open example.xml"), ".", tr("Example File (*example.xml)"));
    qDebug()<<fileName;
    if (fileName.length()>0){
        bxth->addRequest(QString("create db test0815 %1").arg(fileName));
        bxth->addRequest("xquery //book");
        bxth->addRequest("close");
        bxth->addRequest("drop database test0815");
    }
}
