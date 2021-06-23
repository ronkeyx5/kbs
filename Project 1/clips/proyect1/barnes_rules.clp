(defrule apple_macbook (venta (nombre_prod "MacBook Pro 2019")) => (printout t "20% de Descuento @"))
(defrule rtx_3080 (venta (metodo_pago "Contado") ) => (printout t "10% de Descuento @"))
(defrule bono_5 ?a <- (venta (precio ?p) (metodo_pago "Credito")) (test(> ?p 19999)) => (printout t "12 Meses sin Intereses @"))