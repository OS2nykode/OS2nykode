// ------------------------------------------------------------------------------
//  <autogenerated>
//      This code was generated by a tool.
//      Mono Runtime Version: 4.0.30319.42000
// 
//      Changes to this file may cause incorrect behavior and will be lost if 
//      the code is regenerated.
//  </autogenerated>
// ------------------------------------------------------------------------------

// 
//This source code was auto-generated by MonoXSD
//
namespace Schemas {
    
    
    /// <remarks/>
    [System.CodeDom.Compiler.GeneratedCodeAttribute("xsd", "0.0.0.0")]
    [System.SerializableAttribute()]
    [System.Diagnostics.DebuggerStepThroughAttribute()]
    [System.ComponentModel.DesignerCategoryAttribute("code")]
    [System.Xml.Serialization.XmlTypeAttribute(AnonymousType=true, Namespace="http://www.cpr.dk")]
    [System.Xml.Serialization.XmlRootAttribute(Namespace="http://www.cpr.dk", IsNullable=false)]
    public partial class root {
        
        private rootGctp gctpField;
        
        /// <remarks/>
        public rootGctp Gctp {
            get {
                return this.gctpField;
            }
            set {
                this.gctpField = value;
            }
        }
    }
    
    /// <remarks/>
    [System.CodeDom.Compiler.GeneratedCodeAttribute("xsd", "0.0.0.0")]
    [System.SerializableAttribute()]
    [System.Diagnostics.DebuggerStepThroughAttribute()]
    [System.ComponentModel.DesignerCategoryAttribute("code")]
    [System.Xml.Serialization.XmlTypeAttribute(AnonymousType=true, Namespace="http://www.cpr.dk")]
    public partial class rootGctp {
        
        private rootGctpSystem systemField;
        
        private float vField;
        
        private bool vFieldSpecified;
        
        /// <remarks/>
        public rootGctpSystem System {
            get {
                return this.systemField;
            }
            set {
                this.systemField = value;
            }
        }
        
        /// <remarks/>
        [System.Xml.Serialization.XmlAttributeAttribute()]
        public float v {
            get {
                return this.vField;
            }
            set {
                this.vField = value;
            }
        }
        
        /// <remarks/>
        [System.Xml.Serialization.XmlIgnoreAttribute()]
        public bool vSpecified {
            get {
                return this.vFieldSpecified;
            }
            set {
                this.vFieldSpecified = value;
            }
        }
    }
    
    /// <remarks/>
    [System.CodeDom.Compiler.GeneratedCodeAttribute("xsd", "0.0.0.0")]
    [System.SerializableAttribute()]
    [System.Diagnostics.DebuggerStepThroughAttribute()]
    [System.ComponentModel.DesignerCategoryAttribute("code")]
    [System.Xml.Serialization.XmlTypeAttribute(AnonymousType=true, Namespace="http://www.cpr.dk")]
    public partial class rootGctpSystem {
        
        private rootGctpSystemService serviceField;
        
        private string rField;
        
        /// <remarks/>
        public rootGctpSystemService Service {
            get {
                return this.serviceField;
            }
            set {
                this.serviceField = value;
            }
        }
        
        /// <remarks/>
        [System.Xml.Serialization.XmlAttributeAttribute()]
        public string r {
            get {
                return this.rField;
            }
            set {
                this.rField = value;
            }
        }
    }
    
    /// <remarks/>
    [System.CodeDom.Compiler.GeneratedCodeAttribute("xsd", "0.0.0.0")]
    [System.SerializableAttribute()]
    [System.Diagnostics.DebuggerStepThroughAttribute()]
    [System.ComponentModel.DesignerCategoryAttribute("code")]
    [System.Xml.Serialization.XmlTypeAttribute(AnonymousType=true, Namespace="http://www.cpr.dk")]
    public partial class rootGctpSystemService {
        
        private rootGctpSystemServiceCprServiceHeader cprServiceHeaderField;
        
        private rootGctpSystemServiceCprData cprDataField;
        
        private rootGctpSystemServiceKvit kvitField;
        
        private string rField;
        
        /// <remarks/>
        public rootGctpSystemServiceCprServiceHeader CprServiceHeader {
            get {
                return this.cprServiceHeaderField;
            }
            set {
                this.cprServiceHeaderField = value;
            }
        }
        
        /// <remarks/>
        public rootGctpSystemServiceCprData CprData {
            get {
                return this.cprDataField;
            }
            set {
                this.cprDataField = value;
            }
        }
        
        /// <remarks/>
        public rootGctpSystemServiceKvit Kvit {
            get {
                return this.kvitField;
            }
            set {
                this.kvitField = value;
            }
        }
        
        /// <remarks/>
        [System.Xml.Serialization.XmlAttributeAttribute()]
        public string r {
            get {
                return this.rField;
            }
            set {
                this.rField = value;
            }
        }
    }
    
    /// <remarks/>
    [System.CodeDom.Compiler.GeneratedCodeAttribute("xsd", "0.0.0.0")]
    [System.SerializableAttribute()]
    [System.Diagnostics.DebuggerStepThroughAttribute()]
    [System.ComponentModel.DesignerCategoryAttribute("code")]
    [System.Xml.Serialization.XmlTypeAttribute(AnonymousType=true, Namespace="http://www.cpr.dk")]
    public partial class rootGctpSystemServiceCprServiceHeader {
        
        private string rField;
        
        private string valueField;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlAttributeAttribute()]
        public string r {
            get {
                return this.rField;
            }
            set {
                this.rField = value;
            }
        }
        
        /// <remarks/>
        [System.Xml.Serialization.XmlTextAttribute()]
        public string Value {
            get {
                return this.valueField;
            }
            set {
                this.valueField = value;
            }
        }
    }
    
    /// <remarks/>
    [System.CodeDom.Compiler.GeneratedCodeAttribute("xsd", "0.0.0.0")]
    [System.SerializableAttribute()]
    [System.Diagnostics.DebuggerStepThroughAttribute()]
    [System.ComponentModel.DesignerCategoryAttribute("code")]
    [System.Xml.Serialization.XmlTypeAttribute(AnonymousType=true, Namespace="http://www.cpr.dk")]
    public partial class rootGctpSystemServiceCprData {
        
        private rootGctpSystemServiceCprDataRolle rolleField;
        
        private string uField;
        
        /// <remarks/>
        public rootGctpSystemServiceCprDataRolle Rolle {
            get {
                return this.rolleField;
            }
            set {
                this.rolleField = value;
            }
        }
        
        /// <remarks/>
        [System.Xml.Serialization.XmlAttributeAttribute()]
        public string u {
            get {
                return this.uField;
            }
            set {
                this.uField = value;
            }
        }
    }
    
    /// <remarks/>
    [System.CodeDom.Compiler.GeneratedCodeAttribute("xsd", "0.0.0.0")]
    [System.SerializableAttribute()]
    [System.Diagnostics.DebuggerStepThroughAttribute()]
    [System.ComponentModel.DesignerCategoryAttribute("code")]
    [System.Xml.Serialization.XmlTypeAttribute(AnonymousType=true, Namespace="http://www.cpr.dk")]
    public partial class rootGctpSystemServiceCprDataRolle {
        
        private rootGctpSystemServiceCprDataRolleTable tableField;
        
        private string rField;
        
        /// <remarks/>
        public rootGctpSystemServiceCprDataRolleTable Table {
            get {
                return this.tableField;
            }
            set {
                this.tableField = value;
            }
        }
        
        /// <remarks/>
        [System.Xml.Serialization.XmlAttributeAttribute()]
        public string r {
            get {
                return this.rField;
            }
            set {
                this.rField = value;
            }
        }
    }
    
    /// <remarks/>
    [System.CodeDom.Compiler.GeneratedCodeAttribute("xsd", "0.0.0.0")]
    [System.SerializableAttribute()]
    [System.Diagnostics.DebuggerStepThroughAttribute()]
    [System.ComponentModel.DesignerCategoryAttribute("code")]
    [System.Xml.Serialization.XmlTypeAttribute(AnonymousType=true, Namespace="http://www.cpr.dk")]
    public partial class rootGctpSystemServiceCprDataRolleTable {
        
        private rootGctpSystemServiceCprDataRolleTableRow[] rowField;
        
        private string rField;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute("Row")]
        public rootGctpSystemServiceCprDataRolleTableRow[] Row {
            get {
                return this.rowField;
            }
            set {
                this.rowField = value;
            }
        }
        
        /// <remarks/>
        [System.Xml.Serialization.XmlAttributeAttribute()]
        public string r {
            get {
                return this.rField;
            }
            set {
                this.rField = value;
            }
        }
    }
    
    /// <remarks/>
    [System.CodeDom.Compiler.GeneratedCodeAttribute("xsd", "0.0.0.0")]
    [System.SerializableAttribute()]
    [System.Diagnostics.DebuggerStepThroughAttribute()]
    [System.ComponentModel.DesignerCategoryAttribute("code")]
    [System.Xml.Serialization.XmlTypeAttribute(AnonymousType=true, Namespace="http://www.cpr.dk")]
    public partial class rootGctpSystemServiceCprDataRolleTableRow {
        
        private rootGctpSystemServiceCprDataRolleTableRowField[] fieldField;
        
        private sbyte kField;
        
        private bool kFieldSpecified;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute("Field")]
        public rootGctpSystemServiceCprDataRolleTableRowField[] Field {
            get {
                return this.fieldField;
            }
            set {
                this.fieldField = value;
            }
        }
        
        /// <remarks/>
        [System.Xml.Serialization.XmlAttributeAttribute()]
        public sbyte k {
            get {
                return this.kField;
            }
            set {
                this.kField = value;
            }
        }
        
        /// <remarks/>
        [System.Xml.Serialization.XmlIgnoreAttribute()]
        public bool kSpecified {
            get {
                return this.kFieldSpecified;
            }
            set {
                this.kFieldSpecified = value;
            }
        }
    }
    
    /// <remarks/>
    [System.CodeDom.Compiler.GeneratedCodeAttribute("xsd", "0.0.0.0")]
    [System.SerializableAttribute()]
    [System.Diagnostics.DebuggerStepThroughAttribute()]
    [System.ComponentModel.DesignerCategoryAttribute("code")]
    [System.Xml.Serialization.XmlTypeAttribute(AnonymousType=true, Namespace="http://www.cpr.dk")]
    public partial class rootGctpSystemServiceCprDataRolleTableRowField {
        
        private string rField;
        
        private string vField;
        
        private string valueField;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlAttributeAttribute()]
        public string r {
            get {
                return this.rField;
            }
            set {
                this.rField = value;
            }
        }
        
        /// <remarks/>
        [System.Xml.Serialization.XmlAttributeAttribute()]
        public string v {
            get {
                return this.vField;
            }
            set {
                this.vField = value;
            }
        }
        
        /// <remarks/>
        [System.Xml.Serialization.XmlTextAttribute()]
        public string Value {
            get {
                return this.valueField;
            }
            set {
                this.valueField = value;
            }
        }
    }
    
    /// <remarks/>
    [System.CodeDom.Compiler.GeneratedCodeAttribute("xsd", "0.0.0.0")]
    [System.SerializableAttribute()]
    [System.Diagnostics.DebuggerStepThroughAttribute()]
    [System.ComponentModel.DesignerCategoryAttribute("code")]
    [System.Xml.Serialization.XmlTypeAttribute(AnonymousType=true, Namespace="http://www.cpr.dk")]
    public partial class rootGctpSystemServiceKvit {
        
        private string rField;
        
        private string tField;
        
        private sbyte vField;
        
        private bool vFieldSpecified;
        
        private string valueField;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlAttributeAttribute()]
        public string r {
            get {
                return this.rField;
            }
            set {
                this.rField = value;
            }
        }
        
        /// <remarks/>
        [System.Xml.Serialization.XmlAttributeAttribute()]
        public string t {
            get {
                return this.tField;
            }
            set {
                this.tField = value;
            }
        }
        
        /// <remarks/>
        [System.Xml.Serialization.XmlAttributeAttribute()]
        public sbyte v {
            get {
                return this.vField;
            }
            set {
                this.vField = value;
            }
        }
        
        /// <remarks/>
        [System.Xml.Serialization.XmlIgnoreAttribute()]
        public bool vSpecified {
            get {
                return this.vFieldSpecified;
            }
            set {
                this.vFieldSpecified = value;
            }
        }
        
        /// <remarks/>
        [System.Xml.Serialization.XmlTextAttribute()]
        public string Value {
            get {
                return this.valueField;
            }
            set {
                this.valueField = value;
            }
        }
    }
}
