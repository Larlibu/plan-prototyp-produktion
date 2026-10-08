-- V6__Fix_Product_Image_Column_Type.sql
-- Korrigiert den Spaltentyp der 'image'-Spalte in der 'products'-Tabelle auf LONGTEXT,
-- um Konsistenz mit der JPA-Entität zu gewährleisten.

ALTER TABLE products MODIFY COLUMN image LONGTEXT;