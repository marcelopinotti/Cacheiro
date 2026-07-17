CREATE TABLE produtos (
                          id        BIGSERIAL PRIMARY KEY,
                          nome      VARCHAR(120)  NOT NULL,
                          descricao VARCHAR(500),
                          preco     NUMERIC(10,2) NOT NULL,
                          estoque   INTEGER       NOT NULL DEFAULT 0
);

INSERT INTO produtos (nome, descricao, preco, estoque) VALUES
('Teclado mecânico','Switch brown, ABNT2',349.90,15),
('Mouse gamer','16000 DPI, RGB',189.90, 30),
('Monitor 27"','IPS, 144Hz, QHD',1899.00,8),
('Headset USB','Som 7.1, microfone',259.90, 22),
('Webcam Full HD','1080p 60fps',329.00, 12),
('Mousepad XL','90x40cm, base antiderrapante', 79.90, 50),
('Hub USB-C','7 portas, HDMI 4K',219.90, 18),
('SSD NVMe 1TB','Leitura 5000MB/s',499.00, 25);