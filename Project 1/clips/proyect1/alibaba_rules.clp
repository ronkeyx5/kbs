(defrule 18meses (venta (metodo_pago "Credito") ) => (printout t "12 Meses sin Intereses @"))
(defrule rtx_3080 (venta (nombre_prod "RTX 3080") ) => (printout t "20% de Descuento @"))
(defrule rtx_3060 (venta (nombre_prod "RTX 3060") ) => (printout t "20% de Descuento @"))
(defrule bono_10 ?a <- (venta (precio ?p) (metodo_pago "Contado")) (test(> ?p 40000)) => (printout t "Bono $10,000 @"))