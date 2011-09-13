#include "basexthread.h"


BaseXThread::BaseXThread(QString &host, quint16 port, QObject *parent) :
    QThread(parent), hostName(host),port(port),
    quit(false), Timeout(5*1000), dbuser("admin"), dbpasswd("admin"),
    firstRun(true)
{

    END_RES = QByteArray::fromHex("0000");
    RES_FAIL = QByteArray::fromHex("0001");

}


BaseXThread::~BaseXThread(){
    mutex.lock();
    quit = true;
    cond.wakeOne();
    mutex.unlock();
    qDebug()<<"destroy";
    wait();
}

int BaseXThread::addRequest(QString request){

    QMutexLocker locker(&mutex);
    requestQueue.enqueue(request);

    if (!isRunning())
        start();
    else
        cond.wakeOne();

    return 1;
}


void BaseXThread::run(){

    if (firstRun){
        socket = new QTcpSocket();
        firstRun=false;
    }

    while(!quit){

        mutex.lock();
        QString actualQuery = requestQueue.dequeue();
        mutex.unlock();

        if(!socket->isOpen()){

            socket->connectToHost(hostName, port);

            if (!socket->waitForConnected(Timeout)) {
                emit socketError(socket->error(), socket->errorString());
                return;
            }


            if(!connectToBasex()) {
                return;
            }



        }

        char* q = actualQuery.toUtf8().data();
        QByteArray * ts = new QByteArray();

        socket->write(q, strlen(q)+1);
        if (!socket->waitForBytesWritten(Timeout)) {
            emit socketError(socket->error(), socket->errorString());
            //return false;
        }else{
            bool leave = false;
            bool xqueryError = false;
            while (!leave) {
                if (!socket->waitForReadyRead(Timeout)) {
                    emit socketError(socket->error(), socket->errorString());

                }else{
                    ts->append(socket->readAll());
                    if (ts->endsWith(END_RES)) leave=true;
                    if (ts->endsWith(RES_FAIL)){
                        leave=true;
                        xqueryError=true;
                    }
                }
            }
//            QString resString(*ts);
//            qDebug()<<"res: "<<(xqueryError?"error:":"")<<resString;
            if (xqueryError) emit socketError(1001, "BaseX Error in request: \n"+actualQuery);
        }


        mutex.lock();
        emit newResult(ts);
        if (requestQueue.size()==0) cond.wait(&mutex);
        mutex.unlock();


    }
    if (quit){
        if (socket->isOpen()){
            socket->write("exit",5);
            socket->flush();
        }


    }
}

bool BaseXThread::connectToBasex(){
    // wait for timestamp

    bool leave = false;
    QByteArray ts;
    while (!leave) {
        if (!socket->waitForReadyRead(Timeout)) {
            emit socketError(socket->error(), socket->errorString());
            return false;
        }else{
            ts = socket->readAll();
            ts.chop(1); // skip NullTermintation
            leave = true;
        }
    }


    QByteArray codingArray;
    codingArray.append(QCryptographicHash::hash(dbpasswd.toUtf8(), QCryptographicHash::Md5).toHex());
    codingArray.append(ts);
    codingArray = QCryptographicHash::hash(codingArray, QCryptographicHash::Md5).toHex();

    QByteArray nameArray = dbuser.toUtf8();

    //send name
    char* nameChar = nameArray.data();
    qDebug()<< socket->write(nameChar,strlen(nameChar)+1);

    //send md(md(passwd) + timestamp)
    char* codeChar = codingArray.data();
    qDebug()<< socket->write(codeChar,strlen(codeChar)+1);

    ts.clear();
    if (!socket->waitForReadyRead(Timeout)) {
        emit socketError(socket->error(), socket->errorString());
        return false;
    }else{
        ts.append(socket->readAll());
        if (ts.at(0)=='\0'){
            qDebug()<<"connection ok";
        }else{
            emit socketError(1000, "BASEX user authentification failed.");
            return false;
        }
    }

    return true;

}
