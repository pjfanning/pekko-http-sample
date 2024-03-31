#!/usr/bin/env python3

import socket, ssl, logging, time, sys
import h2.connection
from h2.config import H2Configuration
from hyperframe.frame import SettingsFrame

#target_ip = '192.168.56.109'
#target_port = '443'
target_ip = '127.0.0.1'
target_port = '443'
path = '/hello'

PREAMBLE = b'PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n'

def get_http2_ssl_context():
    ctx = ssl.create_default_context(purpose=ssl.Purpose.SERVER_AUTH)
    ctx.check_hostname = False
    ctx.verify_mode = ssl.CERT_NONE
    ctx.options |= ssl.OP_NO_COMPRESSION
    ctx.options |= (
        ssl.OP_NO_SSLv2 | ssl.OP_NO_SSLv3 | ssl.OP_NO_TLSv1 | ssl.OP_NO_TLSv1_1
    )
    ctx.set_alpn_protocols(["h2"])

    return ctx

def negotiate_tls(tcp_conn, context):
    tls_conn = context.wrap_socket(tcp_conn, server_hostname=target_ip)
    negotiated_protocol = tls_conn.selected_alpn_protocol()
    if negotiated_protocol != "h2":
        raise RuntimeError("Didn't negotiate HTTP/2!")

    return tls_conn


l = logging.Logger(name='test')
ol = logging.StreamHandler(sys.stdout)
ol.setLevel(logging.DEBUG)
l.addHandler(ol)

def main():
    context = get_http2_ssl_context()

    conn = socket.create_connection((target_ip, target_port))

    tls_conn = negotiate_tls(conn, context)

    config = H2Configuration(logger=l) #enable log
    h2_conn = h2.connection.H2Connection(config=config)


    # stream 0
    h2_conn._data_to_send += PREAMBLE
    # The INITIAL_WINDOW_SIZE should be 0 or
    # any small value that should not let the server send all the data in one response
    h2_conn.update_settings({SettingsFrame.INITIAL_WINDOW_SIZE: 0})
    tls_conn.sendall(h2_conn.data_to_send())

    # stream 1
    headers = [
        (':authority', target_ip),
        (':path', path),
        (':scheme', 'https'),
        (':method', 'GET'),
    ]
    h2_conn.send_headers(1, headers, end_stream=True)
    tls_conn.sendall(h2_conn.data_to_send())


    start = time.time()
    while True:
        data = tls_conn.recv(1024)
        if not data:
            break
    end = time.time()
    print("Server closed conn after {}s".format(end-start))

main()

