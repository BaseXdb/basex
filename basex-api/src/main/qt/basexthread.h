#ifndef BASEXTHREAD_H
#define BASEXTHREAD_H

#include <QThread>
#include <QMutex>
#include <QWaitCondition>
#include <QtNetwork>

class BaseXThread : public QThread
{
    Q_OBJECT
public:
    explicit BaseXThread(QString &host, quint16 port, QObject *parent = 0);
    ~BaseXThread();

    int addRequest(QString request);
    void run();


signals:
    void newResult(const QByteArray *res);
    void socketError(int socketError, const QString &message);
    void error(int error, const QString &message);
private:
    QString hostName;
    quint16 port;
    QMutex mutex;
    QWaitCondition cond;
    bool quit;
    bool firstRun;

    QString dbuser;
    QString dbpasswd;
    QQueue<QString> requestQueue;

    QByteArray END_RES;
    QByteArray RES_FAIL;

    QTcpSocket *socket;

    bool connectToBasex();
    const int Timeout;

};

#endif // BASEXTHREAD_H
