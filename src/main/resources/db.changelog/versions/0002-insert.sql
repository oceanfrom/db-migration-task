--changeset TUTSK:1
INSERT INTO car (id, name, price) VALUES
                                      (1, 'Toyota Corolla', 20000),
                                      (2, 'Honda Civic', 22000),
                                      (3, 'Ford Focus', 18000);

--changeset TUTSK:2
INSERT INTO loan (id, name, sum, car_id) VALUES
                                             (1, 'Car Loan 1', 15000, 1),
                                             (2, 'Car Loan 2', 20000, 2),
                                             (3, 'Car Loan 3', 10000, 3),
                                             (4, 'Car Loan 4', NULL, 1);
